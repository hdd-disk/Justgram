"""Freeze the Python bootstrap runtime assets (bootstrap.imy + cacert.pem) from upstream sources.

usage: freeze.py <gradle-plugin-jar> <bootstrap-dir> <out-dir>

  <gradle-plugin-jar>  com.chaquo.python:gradle maven jar; embeds build-packages.zip and cacert.pem
  <bootstrap-dir>      repo directory holding java/ python sources (including fork changes)
  <out-dir>            receives chaquopy/bootstrap.imy and chaquopy/cacert.pem
"""
import importlib.util
import marshal
import os
import sys
import zipfile


def check_magic():
    expected = importlib.util.MAGIC_NUMBER
    if sys.version_info[:2] != (3, 11):
        raise SystemExit("bootstrap freeze requires host Python 3.11.x, got %s" % sys.version)
    return expected


def compile_pyc(source_bytes, name, magic, mtime=0):
    code = compile(source_bytes.decode("utf-8"), ".\\" + name.replace("/", "\\"), "exec")
    header = (magic + (0).to_bytes(4, "little") + mtime.to_bytes(4, "little")
              + len(source_bytes).to_bytes(4, "little"))
    return header + marshal.dumps(code)


def main():
    jar_path, bootstrap_dir, out_dir = sys.argv[1:4]
    magic = check_magic()
    jar = zipfile.ZipFile(jar_path)
    packages = zipfile.ZipFile(jar.open("com/chaquo/python/gradle/build-packages.zip"))

    os.makedirs(os.path.join(out_dir, "chaquopy"), exist_ok=True)
    out_path = os.path.join(out_dir, "chaquopy", "bootstrap.imy")

    # Entries derived from the repo source tree: java/**.py -> .pyc, plus static files
    # (vendored package metadata) copied verbatim.
    java_files = sorted(
        os.path.relpath(os.path.join(dp, f), bootstrap_dir).replace(os.sep, "/")
        for dp, _, fns in os.walk(bootstrap_dir) for f in fns
        if f.endswith(".py") and f != "freeze.py"
    )
    java_static = sorted(
        os.path.relpath(os.path.join(dp, f), bootstrap_dir).replace(os.sep, "/")
        for dp, _, fns in os.walk(bootstrap_dir) for f in fns
        if not f.endswith(".py")
    )
    if not java_files:
        raise SystemExit("no .py sources under %s" % bootstrap_dir)

    # Entries derived from upstream build-packages.zip: the vendored pkg_resources tree
    # and its setuptools dist-info metadata, shipped to Python at bootstrap.
    vendored = sorted(i.filename for i in packages.infolist()
                      if not i.is_dir() and (i.filename.startswith("pkg_resources/")
                                             or i.filename.startswith("setuptools-")))
    vendored_pyc = [n for n in vendored if n.endswith(".py")]
    vendored_raw = [n for n in vendored if not n.endswith(".py")]

    dirs = set()
    with zipfile.ZipFile(out_path, "w", zipfile.ZIP_DEFLATED) as out:
        def put_dir_entries(name):
            parts = name.split("/")[:-1]
            for i in range(len(parts)):
                d = "/".join(parts[:i + 1]) + "/"
                if d not in dirs:
                    dirs.add(d)
                    out.writestr(zipfile.ZipInfo(d), b"")

        for name in java_files:
            data = compile_pyc(open(os.path.join(bootstrap_dir, name), "rb").read(),
                               name, magic)
            put_dir_entries(name)
            out.writestr(name[:-3] + ".pyc", data)
        for name in java_static:
            put_dir_entries(name)
            out.writestr(name, open(os.path.join(bootstrap_dir, name), "rb").read())
        for name in vendored_pyc:
            data = compile_pyc(packages.read(name), name, magic)
            put_dir_entries(name)
            out.writestr(name[:-3] + ".pyc", data)
        for name in vendored_raw:
            put_dir_entries(name)
            out.writestr(name, packages.read(name))

    with open(os.path.join(out_dir, "chaquopy", "cacert.pem"), "wb") as f:
        f.write(jar.read("com/chaquo/python/cacert.pem"))

    print("froze %d java pyc + %d vendored pyc + %d metadata files -> %s"
          % (len(java_files), len(vendored_pyc), len(vendored_raw), out_path))


if __name__ == "__main__":
    main()

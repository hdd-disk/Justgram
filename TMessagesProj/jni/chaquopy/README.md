# Chaquopy Python/JNI bridge source

This directory holds the Cython sources for the plugin flavor's Python bridge:

- `java/` — the `chaquopy` Cython extension module (`chaquopy.pyx` + `*.pxi`), built into
  `chaquopy.so`. It is the Python-facing `java` package that drives JNI class reflection,
  method dispatch and the modifier/lookup-plan machinery.
- `chaquopy_java.pyx` — the JNI entry module, built into `libchaquopy_java.so`. It provides
  `Java_com_chaquo_python_Python_startNative` and the `PyObject` JNI bindings.
- `c/android_platform.c` — `redirectStdioToLogcat` (stdout/stderr -> logcat).
- `c/chaquopy_extra.h` / `c/chaquopy_java_extra.h` / `c/alloca.h` — compile-time shims for
  the Cython output. `chaquopy_java_extra.h` must keep `CYTHON_PEP489_MULTI_PHASE_INIT 0`:
  without it the module init is deferred and `startNative` calls unwired capsule pointers
  (NULL jump, SIGSEGV at startup).

## Build

The `buildSrc` tasks `ChaquopyCythonize` / `ChaquopyClangLink` do the work (not CMake):
`python -m cython` produces the C sources under `build/chaquopy/cython/`, applies upstream's
generated-C post-processing (`JNIEXPORT`/`JNICALL` on exported functions, disabled
`__pyx_insert_code_object`), then NDK clang compiles and links directly with
`-O2 -DNDEBUG`. Outputs land in `build/chaquopy/lib/<abi>/`.

**Cython must be exactly 3.0.11** (`pip install Cython==3.0.11`); `ChaquopyCythonize`
enforces this at execution time. Both libraries are generated from the same sources in one
build, so the `JNIRef`/`GlobalRef`/`LocalRef` cdef layouts in `java/chaquopy.pxd` stay
consistent across the two `.so` files.

`libchaquopy_java.so` links libpython via `-L <dir> -lpython3.11` (never the absolute .so
path — the bundled libpython has no SONAME and a direct path link would bake the build
machine path into `DT_NEEDED`). The per-ABI `libpython3.11.so` and its `pyconfig.h` come from
the `com.chaquo.python:target` artifacts, extracted per ABI into `build/chaquopy/target/<abi>/`
(headers are word-size specific: SIZEOF_LONG=4 on armeabi-v7a, 8 on arm64-v8a).

`generateChaquopyBuildJson` regenerates `chaquopy/build.json` with the hashes of the freshly
packaged assets; the generated file overlays the assets srcDir.

Sanity-check the result: `libchaquopy_java.so` must export 27 `Java_com_chaquo_python_*`
symbols and reference `PyModule_Create2` (single-phase init; never
`PyModule_FromDefAndSpec`).

## Provenance

`java/` is a source reconstruction of the Chaquopy fork bundled with the exteraGram plugin
engine; `chaquopy_java.pyx` and `c/android_platform.c` track upstream Chaquopy (MIT,
Copyright (c) Chaquo Ltd.). The content matches the previously verified
startup-working `sdk_chaqsrc` snapshot byte-for-byte (modulo comments).

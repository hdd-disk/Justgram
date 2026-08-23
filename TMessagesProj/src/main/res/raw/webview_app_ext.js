/*
 *  Telegram-Android browser extension for Mini Apps
 *
 *  # Gestures
 *  This script captures whether touch event is consumed by a website, to otherwise apply
 *  down or right gesture. Use `event.preventDefault()` at `touchstart` to prevent those gestures.
 *  It is recommended to do `event.preventDefault()` when dragging or swiping is expected to be
 *  handled by a website.
 *
 *  Since some websites don't do that, the script also captures `style` and `class` changes to
 *  hierarchy of a touch element, and does equivalent of `preventDefault` if those changes happen
 *  while `touchstart` or `touchmove` events.
 */

if (!window.__tg__webview_set) {
    window.__tg__webview_set = true;
    (function () {
        const DEBUG = $DEBUG$;
        const FP_PROTECTION = $FP_PROTECTION$;

        if (FP_PROTECTION) {
            try {
                // Canvas Fingerprinting Protection
                const originalToDataURL = HTMLCanvasElement.prototype.toDataURL;
                HTMLCanvasElement.prototype.toDataURL = function (type) {
                    const ctx = this.getContext('2d');
                    if (ctx && (type === undefined || type === 'image/png')) {
                        const style = ctx.fillStyle;
                        ctx.fillStyle = 'rgba(255, 255, 255, 0.01)';
                        ctx.fillRect(0, 0, 1, 1);
                        ctx.fillStyle = style;
                    }
                    return originalToDataURL.apply(this, arguments);
                };

                // WebGL Fingerprinting Protection
                const originalGetParameter = WebGLRenderingContext.prototype.getParameter;
                WebGLRenderingContext.prototype.getParameter = function (parameter) {
                    if (parameter === 0x9245) return 'Google Inc. (Intel)';
                    if (parameter === 0x9246) return 'ANGLE (Intel, Intel(R) UHD Graphics 620 Direct3D11 vs_5_0 ps_5_0)';
                    if (parameter === 0x1F00) return 'WebKit';
                    if (parameter === 0x1F01) return 'WebKit WebGL';
                    return originalGetParameter.apply(this, arguments);
                };

                // Audio Fingerprinting Protection
                const originalGetChannelData = AudioBuffer.prototype.getChannelData;
                AudioBuffer.prototype.getChannelData = function () {
                    const result = originalGetChannelData.apply(this, arguments);
                    for (let i = 0; i < result.length; i += 100) {
                        result[i] += (Math.random() - 0.5) * 1e-7;
                    }
                    return result;
                };

                // Navigator Hardening
                Object.defineProperty(navigator, 'hardwareConcurrency', { get: () => 4 });
                Object.defineProperty(navigator, 'deviceMemory', { get: () => 8 });
                Object.defineProperty(navigator, 'platform', { get: () => 'Linux armv8l' });
            } catch (e) {
                if (DEBUG) console.error('tgbrowser fp protection error', e);
            }
        }

        // Touch gestures hacks
        let prevented = false;
        let awaitingResponse = false;
        let touchElement = null;
        let mutatedWhileTouch = false;
        let whiletouchstart = false, whiletouchmove = false;
        document.addEventListener('touchstart', e => {
            touchElement = e.target;
            awaitingResponse = true;
            whiletouchstart = true;
        }, true);
        document.addEventListener('touchstart', e => {
            whiletouchstart = false;
        }, false);
        const atLeft = e => !e || e == document || e.scrollLeft <= 0 && atLeft(e.parentNode);
        const atTop = e => !e || e == document || e.scrollTop <= 0 && atTop(e.parentNode);
        document.addEventListener('touchmove', e => {
            whiletouchstart = false;
            whiletouchmove = true;
            if (awaitingResponse) {
                setTimeout(() => {
                    if (awaitingResponse) {
                        if (window.TelegramWebviewProxy) {
                            const allowScrollX = !prevented && atLeft(e.target) && (!window.visualViewport || window.visualViewport.offsetLeft == 0) && !mutatedWhileTouch;
                            const allowScrollY = !prevented && atTop(e.target)  && (!window.visualViewport || window.visualViewport.offsetTop == 0)  && !mutatedWhileTouch;
                            if (DEBUG) {
                                console.log('tgbrowser allowScroll sent after "touchmove": x=' + allowScrollX + ' y=' + allowScrollY, { e, prevented, mutatedWhileTouch });
                            }
                            window.TelegramWebviewProxy.postEvent('web_app_allow_scroll', JSON.stringify([ allowScrollX, allowScrollY ]));
                        }
                        prevented = false;
                        awaitingResponse = false;
                    }
                    mutatedWhileTouch = false;
                }, 16);
            }
        }, true);
        document.addEventListener('touchmove', e => {
            whiletouchmove = false;
        }, false);
        document.addEventListener('scroll', e => {
            if (!e.target) return;
            const allowScrollX = e.target.scrollLeft == 0 && (!window.visualViewport || window.visualViewport.offsetLeft == 0) && !prevented && !mutatedWhileTouch;
            const allowScrollY = e.target.scrollTop == 0  && (!window.visualViewport || window.visualViewport.offsetTop == 0)  && !prevented && !mutatedWhileTouch;
            if (DEBUG) {
                console.log('tgbrowser scroll on' + e.target + ' scrollLeft=' + e.target.scrollLeft + ' scrollTop=' + e.target.scrollTop);
            }
            if (awaitingResponse) {
                if (window.TelegramWebviewProxy) {
                    if (DEBUG) {
                        console.log('tgbrowser allowScroll sent after "scroll": x=' + allowScrollX + ' y=' + allowScrollY, { e, prevented, mutatedWhileTouch, scrollLeft: e.target.scrollLeft, scrollTop: e.target.scrollTop });
                    }
                    window.TelegramWebviewProxy.postEvent('web_app_allow_scroll', JSON.stringify([allowScrollX, allowScrollY]));
                }
                awaitingResponse = false;
            }
            prevented = false;
            mutatedWhileTouch = false;
        }, true);
        if (TouchEvent) {
            const originalPreventDefault = TouchEvent.prototype.preventDefault;
            TouchEvent.prototype.preventDefault = function () {
                prevented = true;
                originalPreventDefault.call(this);
            };
            const originalStopPropagation = TouchEvent.prototype.stopPropagation;
            TouchEvent.prototype.stopPropagation = function () {
                if (this.type === 'touchmove') {
                    whiletouchmove = false;
                } else if (this.type === 'touchstart') {
                    whiletouchstart = false;
                }
                originalStopPropagation.call(this);
            };
        }
        const isParentOf = (e, p) => {
            if (!e || !p) return false;
            if (e == p) return true;
            return isParentOf(e.parentElement, p);
        }
        new MutationObserver(mutationList => {
            const isTouchElement = touchElement && !![...(mutationList||[])]
                .filter(r => r && (r.attributeName === 'style' || r.attributeName === 'class'))
                .map(r => r.target)
                .filter(e => !!e && e != document.body && e != document.documentElement)
                .find(e => isParentOf(touchElement, e));
            if (isTouchElement) { // && (whiletouchstart || whiletouchmove)) {
                if (DEBUG) {
                    console.log('tgbrowser mutation detected', mutationList);
                }
                mutatedWhileTouch = true;
            }
        }).observe(document, { attributes: true, childList: true, subtree: true });
    })();
};
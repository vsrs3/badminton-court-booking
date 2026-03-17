(function (window, document) {
    'use strict';

    if (window.StaffDialog) return;

    var styleId = 'staff-dialog-style';
    var root = null;
    var titleEl = null;
    var messageEl = null;
    var inputWrapEl = null;
    var inputEl = null;
    var cancelBtn = null;
    var okBtn = null;
    var resolver = null;
    var activeMode = 'alert';

    function ensureStyle() {
        if (document.getElementById(styleId)) return;
        var style = document.createElement('style');
        style.id = styleId;
        style.textContent = [
            '.sd-overlay{position:fixed;inset:0;background:rgba(2,6,23,.45);display:flex;align-items:center;justify-content:center;z-index:12000;padding:1rem;}',
            '.sd-overlay.sd-hidden{display:none;}',
            '.sd-modal{width:100%;max-width:420px;background:#fff;border:1px solid #e2e8f0;border-radius:12px;box-shadow:0 20px 60px rgba(2,6,23,.25);overflow:hidden;}',
            '.sd-header{display:flex;align-items:center;gap:.5rem;padding:.9rem 1.1rem;border-bottom:1px solid #e5e7eb;background:#f8fafc;color:#064e3b;}',
            '.sd-title{font-size:.95rem;font-weight:700;margin:0;}',
            '.sd-body{padding:1rem 1.1rem .85rem;color:#334155;font-size:.875rem;line-height:1.45;white-space:pre-line;}',
            '.sd-input-wrap{padding:0 1.1rem 1rem;}',
            '.sd-input-wrap.sd-hidden{display:none;}',
            '.sd-input{width:100%;padding:.6rem .75rem;border:1px solid #cbd5e1;border-radius:8px;font-size:.875rem;}',
            '.sd-input:focus{outline:none;border-color:#064e3b;box-shadow:0 0 0 .2rem rgba(6,78,59,.12);}',
            '.sd-footer{display:flex;justify-content:flex-end;gap:.5rem;padding:.85rem 1.1rem 1rem;}',
            '.sd-btn{border:0;border-radius:8px;padding:.5rem .85rem;font-size:.82rem;font-weight:700;cursor:pointer;}',
            '.sd-btn-cancel{background:#f1f5f9;color:#334155;}',
            '.sd-btn-cancel:hover{background:#e2e8f0;}',
            '.sd-btn-ok{background:#064e3b;color:#fff;}',
            '.sd-btn-ok:hover{background:#065f46;}'
        ].join('');
        document.head.appendChild(style);
    }

    function ensureRoot() {
        if (root) return;
        ensureStyle();

        root = document.createElement('div');
        root.className = 'sd-overlay sd-hidden';
        root.innerHTML =
            '<div class="sd-modal" role="dialog" aria-modal="true">' +
            '  <div class="sd-header"><i class="bi bi-patch-question-fill"></i><h3 class="sd-title"></h3></div>' +
            '  <div class="sd-body"></div>' +
            '  <div class="sd-input-wrap sd-hidden"><input type="text" class="sd-input" /></div>' +
            '  <div class="sd-footer">' +
            '    <button type="button" class="sd-btn sd-btn-cancel">Hủy</button>' +
            '    <button type="button" class="sd-btn sd-btn-ok">Xác nhận</button>' +
            '  </div>' +
            '</div>';

        document.body.appendChild(root);

        titleEl = root.querySelector('.sd-title');
        messageEl = root.querySelector('.sd-body');
        inputWrapEl = root.querySelector('.sd-input-wrap');
        inputEl = root.querySelector('.sd-input');
        cancelBtn = root.querySelector('.sd-btn-cancel');
        okBtn = root.querySelector('.sd-btn-ok');

        cancelBtn.addEventListener('click', function () {
            closeWith(activeMode === 'prompt' ? null : false);
        });
        okBtn.addEventListener('click', function () {
            if (activeMode === 'prompt') {
                closeWith(inputEl.value);
            } else if (activeMode === 'confirm') {
                closeWith(true);
            } else {
                closeWith(undefined);
            }
        });

        root.addEventListener('click', function (ev) {
            if (ev.target === root) {
                closeWith(activeMode === 'prompt' ? null : false);
            }
        });

        document.addEventListener('keydown', function (ev) {
            if (!root || root.classList.contains('sd-hidden')) return;
            if (ev.key === 'Escape') {
                ev.preventDefault();
                closeWith(activeMode === 'prompt' ? null : false);
            }
            if (ev.key === 'Enter' && activeMode === 'prompt' && document.activeElement === inputEl) {
                ev.preventDefault();
                closeWith(inputEl.value);
            }
        });
    }

    function closeWith(value) {
        if (!root || root.classList.contains('sd-hidden')) return;
        root.classList.add('sd-hidden');
        if (resolver) {
            var done = resolver;
            resolver = null;
            done(value);
        }
    }

    function open(config) {
        ensureRoot();
        activeMode = config.mode;
        titleEl.textContent = config.title || (config.mode === 'confirm' ? 'Xác nhận' : 'Thông báo');
        messageEl.textContent = config.message || '';

        var isPrompt = config.mode === 'prompt';
        cancelBtn.textContent = config.cancelText || 'Hủy';
        okBtn.textContent = config.okText || (config.mode === 'alert' ? 'Đã hiểu' : 'Xác nhận');

        cancelBtn.style.display = config.mode === 'alert' ? 'none' : '';
        inputWrapEl.classList.toggle('sd-hidden', !isPrompt);
        if (isPrompt) {
            inputEl.value = config.defaultValue || '';
            inputEl.placeholder = config.placeholder || '';
            setTimeout(function () { inputEl.focus(); inputEl.select(); }, 0);
        } else {
            inputEl.value = '';
            inputEl.placeholder = '';
            setTimeout(function () { okBtn.focus(); }, 0);
        }

        root.classList.remove('sd-hidden');
        return new Promise(function (resolve) { resolver = resolve; });
    }

    function normalize(arg, defaults) {
        if (typeof arg === 'string') {
            var cfg = {};
            for (var k in defaults) cfg[k] = defaults[k];
            cfg.message = arg;
            return cfg;
        }
        var out = {};
        for (var key in defaults) out[key] = defaults[key];
        if (arg && typeof arg === 'object') {
            for (var name in arg) out[name] = arg[name];
        }
        return out;
    }

    window.StaffDialog = {
        alert: function (arg) {
            var cfg = normalize(arg, { mode: 'alert', title: 'Thông báo', message: '', okText: 'Đã hiểu' });
            return open(cfg).then(function () {});
        },
        confirm: function (arg) {
            var cfg = normalize(arg, { mode: 'confirm', title: 'Xác nhận', message: '', okText: 'Xác nhận', cancelText: 'Hủy' });
            return open(cfg).then(function (v) { return !!v; });
        },
        prompt: function (arg) {
            var cfg = normalize(arg, { mode: 'prompt', title: 'Nhập thông tin', message: '', defaultValue: '', placeholder: '', okText: 'Xác nhận', cancelText: 'Hủy' });
            return open(cfg).then(function (v) {
                if (v === null || typeof v === 'undefined') return null;
                return String(v);
            });
        }
    };
})(window, document);

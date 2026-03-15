<%-- NOTE:
     Do not add page contentType directive in this include file.
     Parent JSP pages already define contentType, and duplicated directives may cause compile error.
--%>

<!-- Custom Time Picker Component -->
<style>
    .time-picker-wrapper {
        position: relative;
    }

    .time-picker-display {
        cursor: pointer;
        background-color: #fff;
        border: 1px solid #ced4da;
        border-radius: 0.375rem;
        padding: 0.375rem 0.75rem;
        transition: border-color 0.15s ease-in-out;
    }

    .time-picker-display:focus {
        border-color: #86b7fe;
        outline: 0;
        box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
    }

    .time-picker-display.is-invalid {
        border-color: #dc3545;
    }

    .time-picker-dropdown {
        position: absolute;
        top: 100%;
        left: 0;
        z-index: 1050;
        margin-top: 0.125rem;
        background: white;
        border: 1px solid rgba(0,0,0,.15);
        border-radius: 0.375rem;
        box-shadow: 0 0.5rem 1rem rgba(0,0,0,.175);
        width: 200px;
    }

    .time-picker-dropdown.hidden {
        display: none;
    }

    .time-picker-columns {
        display: flex;
        max-height: 250px;
    }

    .time-picker-column {
        flex: 1;
        overflow-y: auto;
        border-right: 1px solid #dee2e6;
    }

    .time-picker-column:last-child {
        border-right: none;
    }

    .time-picker-column-header {
        position: sticky;
        top: 0;
        background: #f8f9fa;
        padding: 0.5rem;
        text-align: center;
        font-weight: 600;
        border-bottom: 1px solid #dee2e6;
        font-size: 0.875rem;
        z-index: 1;
    }

    .time-picker-item {
        padding: 0.5rem;
        text-align: center;
        cursor: pointer;
        transition: background-color 0.15s;
        font-size: 0.875rem;
    }

    .time-picker-item:hover {
        background-color: #e9ecef;
    }

    .time-picker-item.selected {
        background-color: #0d6efd;
        color: white;
        font-weight: 600;
    }

    .time-picker-icon {
        position: absolute;
        right: 0.75rem;
        top: 50%;
        transform: translateY(-50%);
        pointer-events: none;
        color: #6c757d;
    }
</style>

<script>
/**
 * Custom Time Picker Component
 * Pure HTML + JS implementation with Bootstrap 5
 */
class CustomTimePicker {
    constructor(displayElement, hiddenInput, options = {}) {
        this.display = displayElement;
        this.hiddenInput = hiddenInput;
        this.options = {
            // Changed: 0-24 hours (was 0-23) to support 24:00 for all-day facilities
            hours: Array.from({length: 25}, (_, i) => String(i).padStart(2, '0')),
            minutes: ['00', '30'],
            defaultHour: '00',
            defaultMinute: '00',
            placeholder: 'Ch\u1ecdn th\u1eddi gian',
            required: !!options.required,
            allowedTimes: Array.isArray(options.allowedTimes) && options.allowedTimes.length
                ? options.allowedTimes
                : null
        };

        if (Array.isArray(options.hours) && options.hours.length) {
            this.options.hours = options.hours;
        }
        if (Array.isArray(options.minutes) && options.minutes.length) {
            this.options.minutes = options.minutes;
        }
        if (options.placeholder) {
            this.options.placeholder = options.placeholder;
        }
        if (options.defaultHour) {
            this.options.defaultHour = options.defaultHour;
        }
        if (options.defaultMinute) {
            this.options.defaultMinute = options.defaultMinute;
        }

        this.selectedHour = this.options.defaultHour;
        this.selectedMinute = this.options.defaultMinute;
        this.isOpen = false;

        this.init();
    }

    init() {
        // Create dropdown structure
        this.createDropdown();

        // Set initial value if hidden input has value
        if (this.hiddenInput.value) {
            this.setValue(this.hiddenInput.value);
        } else {
            this.updateDisplay();
        }

        // Event listeners
        this.display.addEventListener('click', () => this.toggle());
        this.display.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                this.toggle();
            }
        });

        // Close on outside click
        document.addEventListener('click', (e) => {
            if (!this.display.contains(e.target) && !this.dropdown.contains(e.target)) {
                this.close();
            }
        });

        // Validation
        if (this.options.required) {
            this.hiddenInput.required = true;
        }
    }

    createDropdown() {
        this.dropdown = document.createElement('div');
        this.dropdown.className = 'time-picker-dropdown hidden';

        const columns = document.createElement('div');
        columns.className = 'time-picker-columns';

        if (this.options.allowedTimes) {
            const timeColumn = this.createColumn('Th\u1eddi gian', this.options.allowedTimes, 'time');
            columns.appendChild(timeColumn);
            this.dropdown.appendChild(columns);
            this.display.parentNode.insertBefore(this.dropdown, this.display.nextSibling);
            return;
        }

        // Hour column
        const hourColumn = this.createColumn('Gi\u1edd', this.options.hours, 'hour');

        // Minute column
        const minuteColumn = this.createColumn('Ph\u00fat', this.options.minutes, 'minute');

        columns.appendChild(hourColumn);
        columns.appendChild(minuteColumn);
        this.dropdown.appendChild(columns);

        // Insert after display element
        this.display.parentNode.insertBefore(this.dropdown, this.display.nextSibling);
    }

    createColumn(title, items, type) {
        const column = document.createElement('div');
        column.className = 'time-picker-column';

        const header = document.createElement('div');
        header.className = 'time-picker-column-header';
        header.textContent = title;
        column.appendChild(header);

        items.forEach(item => {
            const itemEl = document.createElement('div');
            itemEl.className = 'time-picker-item';
            itemEl.textContent = item;
            itemEl.dataset.value = item;

            if (type === 'hour' && item === this.selectedHour) {
                itemEl.classList.add('selected');
            }
            if (type === 'minute' && item === this.selectedMinute) {
                itemEl.classList.add('selected');
            }

            itemEl.addEventListener('click', () => this.selectItem(type, item, itemEl));

            column.appendChild(itemEl);
        });

        return column;
    }

    selectItem(type, value, element) {
        if (type === 'time') {
            const parts = value.split(':');
            this.selectedHour = parts[0];
            this.selectedMinute = parts[1] || '00';
            element.parentNode.querySelectorAll('.time-picker-item').forEach(el => {
                el.classList.remove('selected');
            });
        } else if (type === 'hour') {
            this.selectedHour = value;
            // Remove selected class from all hour items
            element.parentNode.querySelectorAll('.time-picker-item').forEach(el => {
                el.classList.remove('selected');
            });
        } else {
            this.selectedMinute = value;
            // Remove selected class from all minute items
            element.parentNode.querySelectorAll('.time-picker-item').forEach(el => {
                el.classList.remove('selected');
            });
        }

        element.classList.add('selected');
        this.updateValue();
        this.updateDisplay();
    }

    updateValue() {
        // Use string concatenation instead of template literals to avoid JSP EL conflict
        const timeValue = this.selectedHour + ':' + this.selectedMinute;
        this.hiddenInput.value = timeValue;

        // Trigger change event
        const event = new Event('change', { bubbles: true });
        this.hiddenInput.dispatchEvent(event);

        // Remove validation error if present
        this.display.classList.remove('is-invalid');
    }

    updateDisplay() {
        if (this.selectedHour && this.selectedMinute) {
            // Use string concatenation instead of template literals
            this.display.innerHTML =
                '<span>' + this.selectedHour + ':' + this.selectedMinute + '</span>' +
                '<i class="bi bi-clock time-picker-icon"></i>';
        } else {
            this.display.innerHTML =
                '<span class="text-muted">' + this.options.placeholder + '</span>' +
                '<i class="bi bi-clock time-picker-icon"></i>';
        }
    }

    setValue(timeString) {
        const parts = timeString.split(':');
        if (parts.length === 2) {
            this.selectedHour = parts[0].padStart(2, '0');
            this.selectedMinute = parts[1].padStart(2, '0');

            if (this.options.allowedTimes) {
                const normalized = this.selectedHour + ':' + this.selectedMinute;
                if (this.options.allowedTimes.indexOf(normalized) === -1) {
                    return;
                }
            }

            this.updateValue();
            this.updateDisplay();

            if (this.dropdown) {
                this.dropdown.querySelectorAll('.time-picker-item').forEach(el => {
                    const selected = this.options.allowedTimes
                        ? el.dataset.value === (this.selectedHour + ':' + this.selectedMinute)
                        : (el.dataset.value === this.selectedHour || el.dataset.value === this.selectedMinute);
                    if (selected) {
                        el.classList.add('selected');
                    } else {
                        el.classList.remove('selected');
                    }
                });
            }
        }
    }

    toggle() {
        if (this.isOpen) {
            this.close();
        } else {
            this.open();
        }
    }

    open() {
        this.dropdown.classList.remove('hidden');
        this.isOpen = true;

        // Scroll selected items into view
        setTimeout(() => {
            const selectedItems = this.dropdown.querySelectorAll('.time-picker-item.selected');
            selectedItems.forEach(item => {
                item.scrollIntoView({ block: 'nearest' });
            });
        }, 0);
    }

    close() {
        this.dropdown.classList.add('hidden');
        this.isOpen = false;
    }

    destroy() {
        if (this.dropdown && this.dropdown.parentNode) {
            this.dropdown.parentNode.removeChild(this.dropdown);
        }
    }
}

// Helper function to initialize time pickers
function initializeTimePicker(displayId, inputId, required = false) {
    const display = document.getElementById(displayId);
    const input = document.getElementById(inputId);

    let options = {};
    if (typeof required === 'object' && required !== null) {
        options = required;
    } else {
        options.required = !!required;
    }

    if (display && input) {
        return new CustomTimePicker(display, input, options);
    }
    return null;
}
</script>

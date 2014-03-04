/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

//@tag opf-prototype


Ext.define("OPF.core.component.SubLabelable", {
    extend: 'Ext.form.Labelable',

    requires: ['Ext.XTemplate'],

    /**
     * @cfg {String/String[]/Ext.XTemplate} labelableRenderTpl
     * The rendering template for the field decorations. Component classes using this mixin should include
     * logic to use this as their {@link Ext.AbstractComponent#renderTpl renderTpl}, and implement the
     * {@link #getSubTplMarkup} method to generate the field body content.
     */
    subLabelableRenderTpl: [
        '<tpl if="!hideLabel && !(!fieldLabel && hideEmptyLabel)">',
            '<label id="{id}-labelEl"<tpl if="inputId"> for="{inputId}"</tpl> class="{labelCls}"',
                '<tpl if="labelStyle"> style="{labelStyle}"</tpl>>',
                '<tpl if="fieldLabel"><span class="main-label">{fieldLabel}{labelSeparator}</span></tpl>',
                '<tpl if="subFieldLabel"><span class="sub-label {subLabelCls}">{subFieldLabel}</span></tpl>',
            '</label>',
        '</tpl>',
        '<div class="{baseBodyCls} {fieldBodyCls}" id="{id}-bodyEl" role="presentation">{subTplMarkup}</div>',
        '<div id="{id}-errorEl" class="{errorMsgCls}" style="display:none"></div>',
        '<div class="{clearCls}" role="presentation"><!-- --></div>',
        {compiled: true, disableFormats: true}
    ],

    /**
     * @cfg {String} subFieldLabel
     * The sub label for the field. It gets appended with the {@link #labelSeparator}, and its position
     * and sizing is determined by the {@link #labelAlign}, {@link #labelWidth}, and {@link #labelPad}
     * configs.
     */
    subFieldLabel: undefined,

    subLabelCls: '',

    /**
     * Returns the sub label for the field. Defaults to simply returning the {@link #subFieldLabel} config. Can be
     * overridden to provide
     * @return {String} The configured field label, or empty string if not defined
     */
    getSubFieldLabel: function() {
        return this.subFieldLabel || '';
    },

    /**
     * @protected
     * Generates the arguments for the field decorations {@link #labelableRenderTpl rendering template}.
     * @return {Object} The template arguments
     */
    getSubLabelableRenderData: function() {
        var me = this,
            labelAlign = me.labelAlign,
            labelCls = me.labelCls,
            labelClsExtra = me.labelClsExtra,
            subLabelCls = me.subLabelCls,
            subLabelClsExtra = me.subLabelClsExtra,
            labelPad = me.labelPad,
            labelStyle;

        // Calculate label styles up front rather than in the Field layout for speed; this
        // is safe because label alignment/width/pad are not expected to change.
        if (labelAlign === 'top') {
            labelStyle = 'margin-bottom:' + labelPad + 'px;';
        } else {
            labelStyle = 'margin-right:' + labelPad + 'px;';
            // Add the width for border-box browsers; will be set by the Field layout for content-box
            if (Ext.isBorderBox) {
                labelStyle += 'width:' + me.labelWidth + 'px;';
            }
        }

        return Ext.copyTo(
            {
                inputId: me.getInputId(),
                fieldLabel: me.getFieldLabel(),
                subFieldLabel: me.getSubFieldLabel(),
                labelCls: labelClsExtra ? labelCls + ' ' + labelClsExtra : labelCls,
                subLabelCls: subLabelClsExtra ? subLabelCls + ' ' + subLabelClsExtra : subLabelCls,
                labelStyle: labelStyle + (me.labelStyle || ''),
                subTplMarkup: me.getSubTplMarkup()
            },
            me,
            'hideLabel,hideEmptyLabel,fieldBodyCls,baseBodyCls,errorMsgCls,clearCls,labelSeparator',
            true
        );
    },

    setFieldLabel: function(label){
        label = label || '';

        var me = this,
            separator = me.labelSeparator,
            labelEl = me.labelEl;

        me.fieldLabel = label;
        if (me.rendered) {
            if (Ext.isEmpty(label) && me.hideEmptyLabel) {
                labelEl.parent().setDisplayed('none');
            } else {
                if (separator) {
                    label = me.trimLabelSeparator() + separator;
                }
                labelEl.update('<span class="main-label">' + label + '</span>');
                labelEl.parent().setDisplayed('');
            }
        }
    },

    trimLabelSeparator: function() {
        var me = this,
            separator = me.labelSeparator,
            label = me.fieldLabel || '',
            lastChar = label.substr(label.length - 1);

        // if the last char is the same as the label separator then slice it off otherwise just return label value
        return lastChar === separator ? label.slice(0, -1) : label;
    }

});

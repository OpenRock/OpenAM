 /**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 ForgeRock AS. All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */

/**
 * @author JKigwana
 */

/*global window, define, $, form2js, _, js2form, document, console */

define("org/forgerock/openam/ui/policy/OperatorRulesView", [
    "org/forgerock/commons/ui/common/main/AbstractView",
    "org/forgerock/commons/ui/common/util/UIUtils",
    "org/forgerock/commons/ui/common/main/EventManager",
    "org/forgerock/commons/ui/common/util/Constants",
    "org/forgerock/commons/ui/common/main/Configuration"
], function(AbstractView, uiUtils, eventManager, constants, conf) {
    var OperatorRulesView = AbstractView.extend({

        noBaseTemplate: true,
        events: {
            'change    select' : 'onSelect',
            'onfocus   select' : 'checkOptions',
            'mousedown select' : 'checkOptions',
            'click     .item-button-panel > .icon-remove' : 'onDelete',
            'keyup     .item-button-panel > .icon-remove' : 'onDelete'
        },
        data: {},
        mode: 'append',

        render: function (operators, callback, element, itemID) {
            this.data = $.extend(true, {}, operators);
            this.data.master = itemID === undefined ? true : false;
            this.data.itemID = itemID;
            this.setElement(element);
            this.$el.html(uiUtils.fillTemplateWithData("templates/policy/OperatorRulesTemplate.html", this.data));

            if (itemID !== undefined) {
                this.setElement('#operator_'+itemID );
            }

            this.delegateEvents();
            this.$el.find("select").focus().trigger("change");

            if (callback) {callback();}
        },

        rebindElement: function() {
            this.delegateEvents();
        },

        onSelect: function(e) {
            e.stopPropagation();
            var item = $(e.currentTarget).parent(),
                value = e.currentTarget.value,
                data = _.find(this.data.operators, function(obj) {
                    return obj.title === value;
                });

            _.each(this.data.operators, function(obj) {
                item.removeClass( obj.title.toLowerCase() );
            });

            item.data('logical',true);
            item.data('title',  data.title);
            item.data('config', data.config);
            item.addClass(value.toLowerCase());
        },

        checkOptions: function(e) {

            var parent = $(e.target).parent(),
                dropbox = parent.children('ol.dropbox'),
                select = dropbox.parent().children('select'),
                option = null;

            if (dropbox.children(':not(.dragged)').length > 1) {
                _.each(this.data.operators, function(obj) {
                    option = select.find('option[value="'+obj.title+'"]');
                    option.prop('disabled', ( obj.config.properties.condition || obj.config.properties.subject ) ? true : false);
                });

            } else {
                select.children().prop('disabled', false);
            }
        },

        onDelete: function(e) {
            e.stopPropagation();
            if (e.type === 'keyup' && e.keyCode !== 13) { return;}
            this.$el.animate({height: 0, paddingTop: 0, paddingBottom: 0,marginTop: 0,marginBottom: 0, opacity:0}, function() {
                this.remove();
            });
        }

        // TODO...
        /*toggleClose: function(e){
            e.stopPropagation();
            var button = $(e.currentTarget),
                dropbox = button.parent().parent().children('.dropbox');

            if(button.hasClass('icon-folder-open')){
                button.removeClass('icon-folder-open').addClass('icon-folder');
                dropbox.addClass('closed');
            }else{
                button.addClass('icon-folder-open').removeClass('icon-folder');
                dropbox.removeClass('closed');
            }
        }*/


    });


    return OperatorRulesView;
});

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

(function(){var c=tinymce.each,a={paste_auto_cleanup_on_paste:true,paste_enable_default_filters:true,paste_block_drop:false,paste_retain_style_properties:"none",paste_strip_class_attributes:"mso",paste_remove_spans:false,paste_remove_styles:false,paste_remove_styles_if_webkit:true,paste_convert_middot_lists:true,paste_convert_headers_to_strong:false,paste_dialog_width:"450",paste_dialog_height:"400",paste_text_use_dialog:false,paste_text_sticky:false,paste_text_sticky_default:false,paste_text_notifyalways:false,paste_text_linebreaktype:"combined",paste_text_replacements:[[/\u2026/g,"..."],[/[\x93\x94\u201c\u201d]/g,'"'],[/[\x60\x91\x92\u2018\u2019]/g,"'"]]};function b(d,e){return d.getParam(e,a[e])}tinymce.create("tinymce.plugins.PastePlugin",{init:function(d,e){var f=this;f.editor=d;f.url=e;f.onPreProcess=new tinymce.util.Dispatcher(f);f.onPostProcess=new tinymce.util.Dispatcher(f);f.onPreProcess.add(f._preProcess);f.onPostProcess.add(f._postProcess);f.onPreProcess.add(function(i,j){d.execCallback("paste_preprocess",i,j)});f.onPostProcess.add(function(i,j){d.execCallback("paste_postprocess",i,j)});d.onKeyDown.addToTop(function(i,j){if(((tinymce.isMac?j.metaKey:j.ctrlKey)&&j.keyCode==86)||(j.shiftKey&&j.keyCode==45)){return false}});d.pasteAsPlainText=b(d,"paste_text_sticky_default");function h(l,j){var k=d.dom,i;f.onPreProcess.dispatch(f,l);l.node=k.create("div",0,l.content);if(tinymce.isGecko){i=d.selection.getRng(true);if(i.startContainer==i.endContainer&&i.startContainer.nodeType==3){if(l.node.childNodes.length===1&&/^(p|h[1-6]|pre)$/i.test(l.node.firstChild.nodeName)&&l.content.indexOf("__MCE_ITEM__")===-1){k.remove(l.node.firstChild,true)}}}f.onPostProcess.dispatch(f,l);l.content=d.serializer.serialize(l.node,{getInner:1,forced_root_block:""});if((!j)&&(d.pasteAsPlainText)){f._insertPlainText(l.content);if(!b(d,"paste_text_sticky")){d.pasteAsPlainText=false;d.controlManager.setActive("pastetext",false)}}else{f._insert(l.content)}}d.addCommand("mceInsertClipboardContent",function(i,j){h(j,true)});if(!b(d,"paste_text_use_dialog")){d.addCommand("mcePasteText",function(j,i){var k=tinymce.util.Cookie;d.pasteAsPlainText=!d.pasteAsPlainText;d.controlManager.setActive("pastetext",d.pasteAsPlainText);if((d.pasteAsPlainText)&&(!k.get("tinymcePasteText"))){if(b(d,"paste_text_sticky")){d.windowManager.alert(d.translate("paste.plaintext_mode_sticky"))}else{d.windowManager.alert(d.translate("paste.plaintext_mode"))}if(!b(d,"paste_text_notifyalways")){k.set("tinymcePasteText","1",new Date(new Date().getFullYear()+1,12,31))}}})}d.addButton("pastetext",{title:"paste.paste_text_desc",cmd:"mcePasteText"});d.addButton("selectall",{title:"paste.selectall_desc",cmd:"selectall"});function g(s){var l,p,j,t,k=d.selection,o=d.dom,q=d.getBody(),i,r;if(s.clipboardData||o.doc.dataTransfer){r=(s.clipboardData||o.doc.dataTransfer).getData("Text");if(d.pasteAsPlainText){s.preventDefault();h({content:o.encode(r).replace(/\r?\n/g,"<br />")});return}}if(o.get("_mcePaste")){return}l=o.add(q,"div",{id:"_mcePaste","class":"mcePaste","data-mce-bogus":"1"},"\uFEFF\uFEFF");if(q!=d.getDoc().body){i=o.getPos(d.selection.getStart(),q).y}else{i=q.scrollTop+o.getViewPort(d.getWin()).y}o.setStyles(l,{position:"absolute",left:tinymce.isGecko?-40:0,top:i-25,width:1,height:1,overflow:"hidden"});if(tinymce.isIE){t=k.getRng();j=o.doc.body.createTextRange();j.moveToElementText(l);j.execCommand("Paste");o.remove(l);if(l.innerHTML==="\uFEFF\uFEFF"){d.execCommand("mcePasteWord");s.preventDefault();return}k.setRng(t);k.setContent("");setTimeout(function(){h({content:l.innerHTML})},0);return tinymce.dom.Event.cancel(s)}else{function m(n){n.preventDefault()}o.bind(d.getDoc(),"mousedown",m);o.bind(d.getDoc(),"keydown",m);p=d.selection.getRng();l=l.firstChild;j=d.getDoc().createRange();j.setStart(l,0);j.setEnd(l,2);k.setRng(j);window.setTimeout(function(){var u="",n;if(!o.select("div.mcePaste > div.mcePaste").length){n=o.select("div.mcePaste");c(n,function(w){var v=w.firstChild;if(v&&v.nodeName=="DIV"&&v.style.marginTop&&v.style.backgroundColor){o.remove(v,1)}c(o.select("span.Apple-style-span",w),function(x){o.remove(x,1)});c(o.select("br[data-mce-bogus]",w),function(x){o.remove(x)});if(w.parentNode.className!="mcePaste"){u+=w.innerHTML}})}else{u="<p>"+o.encode(r).replace(/\r?\n\r?\n/g,"</p><p>").replace(/\r?\n/g,"<br />")+"</p>"}c(o.select("div.mcePaste"),function(v){o.remove(v)});if(p){k.setRng(p)}h({content:u});o.unbind(d.getDoc(),"mousedown",m);o.unbind(d.getDoc(),"keydown",m)},0)}}if(b(d,"paste_auto_cleanup_on_paste")){if(tinymce.isOpera||/Firefox\/2/.test(navigator.userAgent)){d.onKeyDown.addToTop(function(i,j){if(((tinymce.isMac?j.metaKey:j.ctrlKey)&&j.keyCode==86)||(j.shiftKey&&j.keyCode==45)){g(j)}})}else{d.onPaste.addToTop(function(i,j){return g(j)})}}d.onInit.add(function(){d.controlManager.setActive("pastetext",d.pasteAsPlainText);if(b(d,"paste_block_drop")){d.dom.bind(d.getBody(),["dragend","dragover","draggesture","dragdrop","drop","drag"],function(i){i.preventDefault();i.stopPropagation();return false})}});f._legacySupport()},getInfo:function(){return{longname:"Paste text/word",author:"Moxiecode Systems AB",authorurl:"http://tinymce.moxiecode.com",infourl:"http://wiki.moxiecode.com/index.php/TinyMCE:Plugins/paste",version:tinymce.majorVersion+"."+tinymce.minorVersion}},_preProcess:function(g,e){var k=this.editor,j=e.content,p=tinymce.grep,n=tinymce.explode,f=tinymce.trim,l,i;function d(h){c(h,function(o){if(o.constructor==RegExp){j=j.replace(o,"")}else{j=j.replace(o[0],o[1])}})}if(k.settings.paste_enable_default_filters==false){return}if(tinymce.isIE&&document.documentMode>=9&&/<(h[1-6r]|p|div|address|pre|form|table|tbody|thead|tfoot|th|tr|td|li|ol|ul|caption|blockquote|center|dl|dt|dd|dir|fieldset)/.test(e.content)){d([[/(?:<br>&nbsp;[\s\r\n]+|<br>)*(<\/?(h[1-6r]|p|div|address|pre|form|table|tbody|thead|tfoot|th|tr|td|li|ol|ul|caption|blockquote|center|dl|dt|dd|dir|fieldset)[^>]*>)(?:<br>&nbsp;[\s\r\n]+|<br>)*/g,"$1"]]);d([[/<br><br>/g,"<BR><BR>"],[/<br>/g," "],[/<BR><BR>/g,"<br>"]])}if(/class="?Mso|style="[^"]*\bmso-|w:WordDocument/i.test(j)||e.wordContent){e.wordContent=true;d([/^\s*(&nbsp;)+/gi,/(&nbsp;|<br[^>]*>)+\s*$/gi]);if(b(k,"paste_convert_headers_to_strong")){j=j.replace(/<p [^>]*class="?MsoHeading"?[^>]*>(.*?)<\/p>/gi,"<p><strong>$1</strong></p>")}if(b(k,"paste_convert_middot_lists")){d([[/<!--\[if !supportLists\]-->/gi,"$&__MCE_ITEM__"],[/(<span[^>]+(?:mso-list:|:\s*symbol)[^>]+>)/gi,"$1__MCE_ITEM__"],[/(<p[^>]+(?:MsoListParagraph)[^>]+>)/gi,"$1__MCE_ITEM__"]])}d([/<!--[\s\S]+?-->/gi,/<(!|script[^>]*>.*?<\/script(?=[>\s])|\/?(\?xml(:\w+)?|img|meta|link|style|\w:\w+)(?=[\s\/>]))[^>]*>/gi,[/<(\/?)s>/gi,"<$1strike>"],[/&nbsp;/gi,"\u00a0"]]);do{l=j.length;j=j.replace(/(<[a-z][^>]*\s)(?:id|name|language|type|on\w+|\w+:\w+)=(?:"[^"]*"|\w+)\s?/gi,"$1")}while(l!=j.length);if(b(k,"paste_retain_style_properties").replace(/^none$/i,"").length==0){j=j.replace(/<\/?span[^>]*>/gi,"")}else{d([[/<span\s+style\s*=\s*"\s*mso-spacerun\s*:\s*yes\s*;?\s*"\s*>([\s\u00a0]*)<\/span>/gi,function(o,h){return(h.length>0)?h.replace(/./," ").slice(Math.floor(h.length/2)).split("").join("\u00a0"):""}],[/(<[a-z][^>]*)\sstyle="([^"]*)"/gi,function(t,h,r){var u=[],o=0,q=n(f(r).replace(/&quot;/gi,"'"),";");c(q,function(s){var w,y,z=n(s,":");function x(A){return A+((A!=="0")&&(/\d$/.test(A)))?"px":""}if(z.length==2){w=z[0].toLowerCase();y=z[1].toLowerCase();switch(w){case"mso-padding-alt":case"mso-padding-top-alt":case"mso-padding-right-alt":case"mso-padding-bottom-alt":case"mso-padding-left-alt":case"mso-margin-alt":case"mso-margin-top-alt":case"mso-margin-right-alt":case"mso-margin-bottom-alt":case"mso-margin-left-alt":case"mso-table-layout-alt":case"mso-height":case"mso-width":case"mso-vertical-align-alt":u[o++]=w.replace(/^mso-|-alt$/g,"")+":"+x(y);return;case"horiz-align":u[o++]="text-align:"+y;return;case"vert-align":u[o++]="vertical-align:"+y;return;case"font-color":case"mso-foreground":u[o++]="color:"+y;return;case"mso-background":case"mso-highlight":u[o++]="background:"+y;return;case"mso-default-height":u[o++]="min-height:"+x(y);return;case"mso-default-width":u[o++]="min-width:"+x(y);return;case"mso-padding-between-alt":u[o++]="border-collapse:separate;border-spacing:"+x(y);return;case"text-line-through":if((y=="single")||(y=="double")){u[o++]="text-decoration:line-through"}return;case"mso-zero-height":if(y=="yes"){u[o++]="display:none"}return}if(/^(mso|column|font-emph|lang|layout|line-break|list-image|nav|panose|punct|row|ruby|sep|size|src|tab-|table-border|text-(?!align|decor|indent|trans)|top-bar|version|vnd|word-break)/.test(w)){return}u[o++]=w+":"+z[1]}});if(o>0){return h+' style="'+u.join(";")+'"'}else{return h}}]])}}if(b(k,"paste_convert_headers_to_strong")){d([[/<h[1-6][^>]*>/gi,"<p><strong>"],[/<\/h[1-6][^>]*>/gi,"</strong></p>"]])}d([[/Version:[\d.]+\nStartHTML:\d+\nEndHTML:\d+\nStartFragment:\d+\nEndFragment:\d+/gi,""]]);i=b(k,"paste_strip_class_attributes");if(i!=="none"){function m(q,o){if(i==="all"){return""}var h=p(n(o.replace(/^(["'])(.*)\1$/,"$2")," "),function(r){return(/^(?!mso)/i.test(r))});return h.length?' class="'+h.join(" ")+'"':""}j=j.replace(/ class="([^"]+)"/gi,m);j=j.replace(/ class=([\-\w]+)/gi,m)}if(b(k,"paste_remove_spans")){j=j.replace(/<\/?span[^>]*>/gi,"")}e.content=j},_postProcess:function(g,i){var f=this,e=f.editor,h=e.dom,d;if(e.settings.paste_enable_default_filters==false){return}if(i.wordContent){c(h.select("a",i.node),function(j){if(!j.href||j.href.indexOf("#_Toc")!=-1){h.remove(j,1)}});if(b(e,"paste_convert_middot_lists")){f._convertLists(g,i)}d=b(e,"paste_retain_style_properties");if((tinymce.is(d,"string"))&&(d!=="all")&&(d!=="*")){d=tinymce.explode(d.replace(/^none$/i,""));c(h.select("*",i.node),function(m){var n={},k=0,l,o,j;if(d){for(l=0;l<d.length;l++){o=d[l];j=h.getStyle(m,o);if(j){n[o]=j;k++}}}h.setAttrib(m,"style","");if(d&&k>0){h.setStyles(m,n)}else{if(m.nodeName=="SPAN"&&!m.className){h.remove(m,true)}}})}}if(b(e,"paste_remove_styles")||(b(e,"paste_remove_styles_if_webkit")&&tinymce.isWebKit)){c(h.select("*[style]",i.node),function(j){j.removeAttribute("style");j.removeAttribute("data-mce-style")})}else{if(tinymce.isWebKit){c(h.select("*",i.node),function(j){j.removeAttribute("data-mce-style")})}}},_convertLists:function(g,e){var i=g.editor.dom,h,l,d=-1,f,m=[],k,j;c(i.select("p",e.node),function(t){var q,u="",s,r,n,o;for(q=t.firstChild;q&&q.nodeType==3;q=q.nextSibling){u+=q.nodeValue}u=t.innerHTML.replace(/<\/?\w+[^>]*>/gi,"").replace(/&nbsp;/g,"\u00a0");if(/^(__MCE_ITEM__)+[\u2022\u00b7\u00a7\u00d8o\u25CF]\s*\u00a0*/.test(u)){s="ul"}if(/^__MCE_ITEM__\s*\w+\.\s*\u00a0+/.test(u)){s="ol"}if(s){f=parseFloat(t.style.marginLeft||0);if(f>d){m.push(f)}if(!h||s!=k){h=i.create(s);i.insertAfter(h,t)}else{if(f>d){h=l.appendChild(i.create(s))}else{if(f<d){n=tinymce.inArray(m,f);o=i.getParents(h.parentNode,s);h=o[o.length-1-n]||h}}}c(i.select("span",t),function(v){var p=v.innerHTML.replace(/<\/?\w+[^>]*>/gi,"");if(s=="ul"&&/^__MCE_ITEM__[\u2022\u00b7\u00a7\u00d8o\u25CF]/.test(p)){i.remove(v)}else{if(/^__MCE_ITEM__[\s\S]*\w+\.(&nbsp;|\u00a0)*\s*/.test(p)){i.remove(v)}}});r=t.innerHTML;if(s=="ul"){r=t.innerHTML.replace(/__MCE_ITEM__/g,"").replace(/^[\u2022\u00b7\u00a7\u00d8o\u25CF]\s*(&nbsp;|\u00a0)+\s*/,"")}else{r=t.innerHTML.replace(/__MCE_ITEM__/g,"").replace(/^\s*\w+\.(&nbsp;|\u00a0)+\s*/,"")}l=h.appendChild(i.create("li",0,r));i.remove(t);d=f;k=s}else{h=d=0}});j=e.node.innerHTML;if(j.indexOf("__MCE_ITEM__")!=-1){e.node.innerHTML=j.replace(/__MCE_ITEM__/g,"")}},_insert:function(f,d){var e=this.editor,g=e.selection.getRng();if(!e.selection.isCollapsed()&&g.startContainer!=g.endContainer){e.getDoc().execCommand("Delete",false,null)}e.execCommand("mceInsertContent",false,f,{skip_undo:d})},_insertPlainText:function(g){var d=this.editor,e=b(d,"paste_text_linebreaktype"),i=b(d,"paste_text_replacements"),f=tinymce.is;function h(j){c(j,function(k){if(k.constructor==RegExp){g=g.replace(k,"")}else{g=g.replace(k[0],k[1])}})}if((typeof(g)==="string")&&(g.length>0)){if(/<(?:p|br|h[1-6]|ul|ol|dl|table|t[rdh]|div|blockquote|fieldset|pre|address|center)[^>]*>/i.test(g)){h([/[\n\r]+/g])}else{h([/\r+/g])}h([[/<\/(?:p|h[1-6]|ul|ol|dl|table|div|blockquote|fieldset|pre|address|center)>/gi,"\n\n"],[/<br[^>]*>|<\/tr>/gi,"\n"],[/<\/t[dh]>\s*<t[dh][^>]*>/gi,"\t"],/<[a-z!\/?][^>]*>/gi,[/&nbsp;/gi," "],[/(?:(?!\n)\s)*(\n+)(?:(?!\n)\s)*/gi,"$1"],[/\n{3,}/g,"\n\n"]]);g=d.dom.decode(tinymce.html.Entities.encodeRaw(g));if(f(i,"array")){h(i)}else{if(f(i,"string")){h(new RegExp(i,"gi"))}}if(e=="none"){h([[/\n+/g," "]])}else{if(e=="br"){h([[/\n/g,"<br />"]])}else{if(e=="p"){h([[/\n+/g,"</p><p>"],[/^(.*<\/p>)(<p>)$/,"<p>$1"]])}else{h([[/\n\n/g,"</p><p>"],[/^(.*<\/p>)(<p>)$/,"<p>$1"],[/\n/g,"<br />"]])}}}d.execCommand("mceInsertContent",false,g)}},_legacySupport:function(){var e=this,d=e.editor;d.addCommand("mcePasteWord",function(){d.windowManager.open({file:e.url+"/pasteword.htm",width:parseInt(b(d,"paste_dialog_width")),height:parseInt(b(d,"paste_dialog_height")),inline:1})});if(b(d,"paste_text_use_dialog")){d.addCommand("mcePasteText",function(){d.windowManager.open({file:e.url+"/pastetext.htm",width:parseInt(b(d,"paste_dialog_width")),height:parseInt(b(d,"paste_dialog_height")),inline:1})})}d.addButton("pasteword",{title:"paste.paste_word_desc",cmd:"mcePasteWord"})}});tinymce.PluginManager.add("paste",tinymce.plugins.PastePlugin)})();
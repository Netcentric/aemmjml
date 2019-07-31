/*
 * The "BSD licence"
 *
 * Copyright (c) 2013 Tom Everett
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

// based on https://github.com/antlr/grammars-v4/tree/master/html

parser grammar MjmlParser;

options { tokenVocab=MjmlLexer; }

mjmlDocument
    : (scriptlet | SEA_WS)* xml? (scriptlet | SEA_WS)* dtd? (scriptlet | SEA_WS)* mjmlElements*
    ;

mjmlElements
    : mjmlMisc* mjmlElement mjmlMisc*
    ;

mjmlElement
    : mjmlStartElement mjmlContent mjmlCloseElement
    | mjmlStartCloseElement
    | mjmlStartElement
    | mjmlSpoolElement
    ;

mjmlStartElement
    : TAG_OPEN mjmlTagName mjmlAttribute* TAG_CLOSE
    ;

mjmlStartCloseElement
    : TAG_OPEN mjmlTagName mjmlAttribute* TAG_SLASH_CLOSE
    ;

mjmlCloseElement
    : TAG_OPEN TAG_SLASH mjmlTagName TAG_CLOSE
    ;

mjmlSpoolElement
    : scriptlet
    // todo: we shouldn't spool <script> and <style> but rather properly process them
    | script
    | style
    ;

mjmlContent
    : mjmlChardata? ((mjmlElement | mjmlCDATA | mjmlComment) mjmlChardata?)*
    ;

mjmlAttribute
    : mjmlAttributeName TAG_EQUALS mjmlAttributeValue
    | mjmlAttributeName
    ;

mjmlAttributeName
    : TAG_NAME
    ;

mjmlAttributeValue
    : ATTVALUE_VALUE
    ;

mjmlTagName
    : TAG_NAME
    ;

mjmlChardata
    : HTML_TEXT
    | SEA_WS
    ;

mjmlMisc
    : mjmlComment
    | SEA_WS
    ;

mjmlComment
    : HTML_COMMENT
    | HTML_CONDITIONAL_COMMENT
    ;

mjmlCDATA
    : CDATA
    ;

dtd
    : DTD
    ;

xml
    : XML_DECLARATION
    ;

scriptlet
    : SCRIPTLET
    ;

script
    : SCRIPT_OPEN ( SCRIPT_BODY | SCRIPT_SHORT_BODY)
    ;

style
    : STYLE_OPEN ( STYLE_BODY | STYLE_SHORT_BODY)
    ;
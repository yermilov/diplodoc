<%@ page import="com.github.dipodoc.webui.admin.domain.data.Doc" %>

<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main">
        <title><g:message message="knu/shevapil" /></title>
    </head>

    <body>
        <div id="show-doc" class="content scaffold-show" role="main">
            <h1>${startingPost.title}</h1>

            <div class="content">
                <a href="${startingPost.uri}" target="_blank"><g:fieldValue bean="${startingPost}" field="uri"/></a> at <g:fieldValue bean="${startingPost}" field="loadTime"/>
            </div>

            <div class="content">
                ${raw(startingPost.meaningHtml)}
            </div>
        </div>
    </body>
</html>

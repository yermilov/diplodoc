<%@ page import="com.github.dipodoc.webui.admin.domain.data.Doc" %>

<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main">
        <title><g:message message="knu/studvote" /></title>
    </head>

    <body>
        <div id="list-doc" class="content scaffold-list" role="main">
            <table>
                <tbody>
                <g:each in="${socialsList}" status="i" var="social">
                    <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                        <td>
                            <h3><a href="${social.uri}" target="_blank">${social.uri}</a></h3>
                            <h4>by <a href="${social.author.uri}" target="_blank">${social.author.name}</a> at ${social.timestamp}</h4>
                            <h5>importancy: ${social.importancy}, sentiment: ${social.sentiment}</h5>

                            <span>${social.text}</span>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
    </body>
</html>

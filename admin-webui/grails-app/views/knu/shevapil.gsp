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
                <span>${raw(startingPost.meaningHtml)}</span>
            </div>
        </div>

        <div id="list-doc" class="content scaffold-list" role="main">
            <table>
				<tbody>
				<g:each in="${relatedSocialsList}" status="i" var="relatedSocial">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
						<td>
							<h3><a href="${relatedSocial.uri}" target="_blank">${relatedSocial.uri}</a></h3>
                            <h4>by <a href="${relatedSocial.author.uri}" target="_blank">${relatedSocial.author.name}</a> at ${relatedSocial.timestamp}</h4>
                            <h5>importancy: ${relatedSocial.importancy}, sentiment: ${relatedSocial.sentiment}</h5>

                            <span>${relatedSocial.text}</span>
						</td>
					</tr>
				</g:each>
				</tbody>
			</table>
		</div>
    </body>
</html>

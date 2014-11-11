<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib prefix="debug" tagdir="/WEB-INF/tags/debug" %>

<%@tag description="Display debugging info." %>

<c:if test="${not empty _DebugInfo}">
    <style>
        #listContainer{
            margin-top:15px;
        }

        #debug_information ul, li {
            list-style: none;
            margin:0;
            padding:0;
            cursor: pointer;
        }
        #debug_information span {
            margin:0;
            display:block;
        }
        #debug_information span:hover {
            background-color:#909090;
        }
        #debug_information li {
            line-height:140%;
            text-indent:0px;
            background-position: 1px 8px;
            padding-left: 20px;
            background-repeat: no-repeat;
        }
        #debug_information li.red {
            color: #97080b;
        }

        #debug_information li.green {
            color: #009600;
        }

        #debug_information li.black {
            color: black;
        }
        /* Collapsed state for list element */
        #debug_information .collapsed {
            cursor:pointer;
            text-decoration: underline;
            list-style-type: circle;
        }
        /* Expanded state for list element
        /* NOTE: This class must be located UNDER the collapsed one */
        #debug_information .expanded {
            cursor:pointer;
            text-decoration: none;
            list-style-type: disc;
        }

    </style>

    <script type="text/javascript">
        function applyLists() {
            $('#debug_information').find('li:has(ul)')
                    .click( function(event) {
                        if (this == event.target) {
                            $(this).toggleClass('expanded');
                            $(this).children('ul').toggle('medium');
                        }
                        return false;
                    })
                    .addClass('collapsed')
                    .children('ul').hide();
        }

        $(document).ready(function() {
            applyLists();
        });
    </script>

    <div id="listContainer">
        <ul id="debug_information">
            <c:forEach items="${_DebugInfo}" var="debugData">
                <c:set var="debugMap" value="${debugData.all}" />
                <c:if test="${not empty debugMap}">
                    <c:choose>
                        <c:when test="${debugMap.Type eq 'METHOD'}">
                            <debug:debugMethod debugMap="${debugMap}" />
                        </c:when>
                        <c:otherwise>
                            <debug:debugUnformatted debugMap="${debugMap}" />
                        </c:otherwise>
                    </c:choose>
                </c:if>
            </c:forEach>
        </ul>
    </div>

</c:if>
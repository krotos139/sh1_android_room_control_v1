<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>

<xsl:template match='/'>
  <html>
  <head>
	  <title>Room control</title>
	  <meta http-equiv='refresh' content='15' url='/control.xml'/>
	  <link rel="stylesheet" type="text/css" href="/style.css"/>
  </head>
  <body>
      <table class='z1'>
      <tr>
        <td>
			<a href='/control.xml' class="z1_button_small">Control</a>
		</td>
        <td>
			<a href='/sensors.xml' class="z1_button_small">Sensors</a>
		</td>
      </tr>
    </table>
    <table class='z1'>
	  <xsl:for-each select='response/outputs/relays/relay'>
      <tr>
        <td class="z1_text"> <xsl:value-of select='@name'/> </td>
        <td>
		  <xsl:if test=". = 0">
			<a href="?cmd=relaycontrol&amp;v=1&amp;id={@id}" class="z1_button_red">OFF</a>
		  </xsl:if>
		  <xsl:if test=". = 1">
			<a href='?cmd=relaycontrol&amp;v=0&amp;id={@id}' class="z1_button_green">ON</a>
		  </xsl:if>
		</td>
		
      </tr>
	  </xsl:for-each>
    </table>
  </body>
  </html>
</xsl:template>
</xsl:stylesheet>


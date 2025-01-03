## SerialPlotter
A cross-platform app for plotting data from and sending commands to a serial port.
[rec.mp4](readmeResources%2Frec.mp4)
## Installation
Check out the <a href="https://seerbird.github.io/SerialPlotter/download.html">download page</a>.
## Usage
After installing and opening the app, you should be able to see a list of available ports in the top left.
After choosing the port you will be able to see plots of the data that arrives to the port on the right side of the UI.
You will also be able to send text to the port using the textbox in the bottom left.
UTF-8 is used to decode and encode text, and to plot data you send to the serial port you need to use the following packet structure:

<code>{plotName(key:value,key:value)plotName(key:value,key:value)}</code>, 

where a plot will be generated for each plotName and the plots will show datasets corresponding to the keys. For example, when receiving packets like 
<code>{plot1(a:value,b:value,c:value)plot2(a:value)}</code>, SerialPlotter will show two plots named plot1 and plot2 with three and one datasets respectively named as the keys.
SerialPlotter will attempt to parse all values as floats, and all packets will be recorded in the log on the left side.

Text received between packets will be logged as well, in quotation marks. Hopefully, any problems with the ports/packets will go to the log.

The textbox that appears in the top right when you connect to a port allows you to choose the baudrate of the port.
The textbox attached to each plot allows you to choose how many of the last values will be plotted per key.

This app uses <a href = "https://github.com/Fazecast/jSerialComm/tree/master?tab=readme-ov-file#jserialcomm">JSerialComm</a> to access the serial ports.
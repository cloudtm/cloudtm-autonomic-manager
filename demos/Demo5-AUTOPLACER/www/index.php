<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <script src="js/excanvas.js" type="text/javascript"></script>
        <script src="js/excanvas.min.js" type="text/javascript"></script>
        <script src="js/jquery.js" type="text/javascript"></script>
        <script src="js/jquery.flot.js" type="text/javascript"></script>
        <title>Workload Monitor [Real Time plots]</title>
        <script type="text/javascript">
            <?php
                $folder = "current";

                if (isset($_REQUEST['apps'])) {
                    $folder = $_REQUEST['apps'];
                }   	

                echo 'var folder = "' . $folder . '";';
                echo 'var folderArray = [';
                $array = split(",", $folder);
                echo '"'.$array[0].'"';

                for ($idx = 1; $idx < count($array); ++$idx) {
                    echo ',"'.$array[$idx].'"';
                }
                echo '];';

                if (isset($_REQUEST['smooth'])) {
                    echo 'var smoothAlpha = ' . $_REQUEST['smooth'] . ';';
                } else {
                    echo 'var smoothAlpha = 1;';
                }
            ?>

            function smooth(oldValue, newValue, alpha) {
                if (oldValue == -1 || alpha >= 1) return newValue;
                if (alpha <= 0) return oldValue;
                return alpha * newValue + (1 - alpha) * oldValue;
            }
        </script>
    </head>
    <body>
        <h1>Workload Monitor - Real Time plots</h1>
        <table style="border:1px solid black;">
            <tr>
                <td><center><b>Throughput (tx/sec)</b></center></td>
                <td><center><b>Number of Reads (avg)</b></center></td>
                <td><center><b>Number of Remote Reads (avg)</b></center></td>
            </tr>
            <tr>
                <td><div id="throughput" style="width:500px;height:300px"></div></td>
                <td><div id="gets" style="width:500px;height:300px"></div></td>
                <td><div id="remoteGets" style="width:500px;height:300px"></div></td>
            </tr>
            <tr>
                <td><center><b>Read Only Tx execution (avg, microseconds)</b></center></td>
                <td><center><b>Write Tx execution time (avg, microseconds)</b></center></td>
                <td><center><b>Commit Latency (avg, microseconds)</b></center></td>
            </tr>
            <tr>
                <td><div id="roExec" style="width:500px;height:300px"></div></td>
                <td><div id="wrExec" style="width:500px;height:300px"></div></td>
                <td><div id="commitLatency" style="width:500px;height:300px"></div></td>
            </tr>
        </table>

        <p>Time between updates:<input id="updateInterval" type="text" value="" style="text-align: right; width:5em"> milliseconds</p>

        <script type="text/javascript">
            $(function () {
                // setup control widge
                var updateInterval = 5000;

                $("#updateInterval").val(updateInterval).change(function () {
                var v = $(this).val();

                if (v && !isNaN(+v)) {
                    updateInterval = +v;
                    if (updateInterval < 500)
                        updateInterval = 500;
                    if (updateInterval > 20000)
                        updateInterval = 20000;
                    $(this).val("" + updateInterval);
                }
            });

            // setup plo
            var default_options = {
                series: { shadowSize: 0 }, // drawing is faster without shadows
                yaxis: { min: 0 },
                xaxis: { min: 0 }
            };

            var log_options = {
                series: { shadowSize: 0 }, // drawing is faster without shadows
                yaxis: { transform: function (v) { if (v == 0) return 0; return Math.log(v); },
                inverseTransform: function (v) { if (v == 0) return 0; return Math.exp(v);},
                ticks: function logTickGenerator(axis) {
                    var res = [], v = 100;
                    do {
                        v = v * 10;
                        res.push(v);
                    } while (v < axis.max);

                    return res;
                }},
                xaxis: { min: 0 }
            };

            function updatePlot(div, param, avg, options) {
                $.ajax({
                    url: "get-data.php?param=" + param + "&avg=" + avg + "&folder=" + folder,
                    method: 'GET',
                    dataType: 'text',
                    success: function(text) {
                        var lines = text.split("\n");
                        var allData = [];
                        var dataIdx = 0;
                        var oldValue = -1;
                        var dataObj = { data: [], color: dataIdx, label: folderArray[dataIdx++]}
                        for(var i = 0, j = 0; i < lines.length; i++) {
                            if (lines[i] == ".") {
                                allData.push(dataObj);
                                dataObj = { data: [] , color: dataIdx, label: folderArray[dataIdx++]};
                                j = 0;
                                continue;
                            }
                            var keyValue = lines[i].split("|");
                            if (keyValue[0] == "" || keyValue[1] == "") continue;
                            oldValue = smooth(oldValue, keyValue[1], smoothAlpha);
                            dataObj.data[j++] = new Array(keyValue[0],oldValue);
                        }
                        $.plot($("#" + div), allData, options);
                    }
                });
            }

            function update() {
                updatePlot("throughput", "throughput", "false", default_options);
                updatePlot("gets", "numberOfGets", "true", default_options);
                updatePlot("remoteGets", "numberOfRemoteGets", "true", default_options);
                updatePlot("roExec", "avgReadOnlyTxDuration", "true", default_options);
                updatePlot("wrExec", "avgWriteTxDuration", "true", default_options);
                updatePlot("commitLatency", "CommitLatency", "true", log_options);
                setTimeout(update, updateInterval);
            }

            update();
            });
        </script>
    </body>
</html>

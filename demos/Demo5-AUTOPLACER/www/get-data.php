<?php

$parameter = "none";
$average = "false";
$minObservation = -1;
$perFileObservation = 0;
$folder = "files";

if (isset($_REQUEST['param'])) {
    $parameter = $_REQUEST['param'];
}

if (isset($_REQUEST['avg'])) {
    $average = $_REQUEST['avg'];
}

if (isset($_REQUEST['folder'])) {
    $folder = $_REQUEST['folder'];
}

foreach (split(",", $folder) as $f) {
    $sum = array();
    $numOfFiles = 0;
    $paramIdx = -1;

    if (!file_exists($f)) {
        echo ".\n";
        continue;
    }

    if ($dir = opendir($f)) {
        while (false !== ($filename = readdir($dir))) {
            if ($filename == "." || $filename == "..")
                continue;

            $handle = fopen($f."/".$filename, "r");

            $line = fgets($handle);
            $newLine = str_replace(' ', '', trim($line));
            $array = split(",", $newLine);
            foreach ($array as $key => $value) {
                if ($value == $parameter) {
                    $paramIdx = $key;
                    break;
                }
            }

            if ($paramIdx == -1) continue;

            while(!feof($handle)) {
                $line = fgets($handle);
                $newLine = str_replace(' ', '', trim($line));
                $array = split(",", $newLine);

                if (!array_key_exists($paramIdx, $array) || $array[$paramIdx] == "") {
                    continue;
                }

                if (!array_key_exists(0, $array) || $array[0] == "") {
                    continue;
                }

                if (array_key_exists($array[0], $sum)) {
                    $sum[$array[0]] += $array[$paramIdx];
                } else {
                    $sum[$array[0]] = $array[$paramIdx];
                }

                if ($array[0] !== "") {
                    $perFileObservation = $array[0];
                }
            }

            if ($minObservation == -1) {
                $minObservation = $perFileObservation;
            } else if ($minObservation > $perFileObservation) {
                $minObservation = $perFileObservation;
            }

            $numOfFiles++;
            fclose($handle);
        }

        closedir($dir);

        if ($average == "true") {
            foreach ($sum as $key => $value) {
                $value /= $numOfFiles;
                echo "$key|$value\n";
            }
        } else {
            foreach ($sum as $key => $value) {
                echo "$key|$value\n";
            }
        }
    }
    echo ".\n";
}
?>

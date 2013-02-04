set terminal postscript eps enhanced monochrome dashed lw 1 "Helvetica" 20
set output 'Throughput.eps'
set xlabel offset character -3, -1, 0 "Number of nodes"
set ylabel offset character 2, -2, 0 "Number of threads"
set zlabel offset character 5, 5, 0 "Throughput"
set pointsize 1.75
set isosample 500
set pm3d
set key outside below 
set view 45,200
splot "gnuplot/data/Throughput.txt" title "" with lines 
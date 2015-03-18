#!/bin/bash
start=0
end=360
beamsize=20.0
## declare an array variable
declare -a arr=(e6p11 e6p22 sch1p11 sch1p22)
declare -a arrchunk=(5 7 10 15 20 30 40 60)
#declare -a arrweight=(2 2.5 3 3.5 5 6 7)
declare -a arrweight=(1.7 1.75 1.8 2 2.5 3 4 5 6 7 8)

for weight in ${arrweight[@]}
do
## now loop through the dataset array
for i in ${arr[@]}
do
# loop through the chunksize array
for j in ${arrchunk[@]}
do
   echo $i $j
   # or do whatever with individual element of the array
   command='./results_affine_same_closed/output_'$i'_'$j'_'$weight'_'$start'_'$end'.txt'
   echo $command
   # modify -go0.125 -ge0.05
   java Main_class -w$weight -c$j -d/home/vax7/u6/inaim/MSA/data/$i -s$start -e$end -b$beamsize -go0.1 -ge0.1 -r1 > $command
done
done
done


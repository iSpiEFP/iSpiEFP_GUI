#!/bin/csh
#SBATCH -o iSpiClient/Libefp/jobs/complex1/output/complex2.err
#SBATCH -A debug
#SBATCH -N 1
#SBATCH -n 20
#SBATCH -t 00:30:00
#SBATCH --mem=0
/depot/lslipche/apps/iSpiEFP/packages/libefp/bin/efpmd iSpiClient/Libefp/jobs/complex1/input/complex2.in > iSpiClient/Libefp/jobs/complex1/output/complex2.out

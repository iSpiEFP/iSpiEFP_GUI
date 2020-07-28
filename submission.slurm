#!/bin/csh
#SBATCH -o iSpiClient/Libefp/jobs/facebook/output/facebook.err
#SBATCH -A debug
#SBATCH -N 1
#SBATCH -n 20
#SBATCH -t 00:30:00
#SBATCH --mem=0
/depot/lslipche/apps/iSpiEFP/packages/libefp/bin/efpmd iSpiClient/Libefp/jobs/facebook/input/facebook.in > iSpiClient/Libefp/jobs/facebook/output/facebook.out

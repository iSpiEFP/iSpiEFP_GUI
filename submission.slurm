#!/bin/csh
#SBATCH -o iSpiClient/Libefp/jobs/complexxTest/output/complexxTest.err
#SBATCH -A debug
#SBATCH -N 1
#SBATCH -n 20
#SBATCH -t 00:30:00
#SBATCH --mem=0
/depot/lslipche/apps/iSpiEFP/packages/libefp/bin/efpmd iSpiClient/Libefp/jobs/complexxTest/input/complexxTest.in > iSpiClient/Libefp/jobs/complexxTest/output/complexxTest.out

#!/bin/csh
#SBATCH -o iSpiClient/Libefp/output/job.stdout
#SBATCH -A standby
#SBATCH -N 1
#SBATCH -n 20
#SBATCH -t 00:00:00
#SBATCH --mem=1024
/depot/lslipche/apps/iSpiEFP/packages/libefp/bin/efpmd iSpiClient/Libefp/input/job.in > iSpiClient/Libefp/output/job.out

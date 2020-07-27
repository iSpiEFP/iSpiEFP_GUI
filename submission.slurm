#!/bin/csh
#SBATCH -o iSpiClient/Libefp/jobs/acetone/output/acetone.err
#SBATCH -A debug
#SBATCH -N 1
#SBATCH -n 20
#SBATCH -t 00:30:00
#SBATCH --mem=0
/depot/lslipche/apps/iSpiEFP/packages/libefp/bin/efpmd iSpiClient/Libefp/jobs/acetone/input/acetone.in > iSpiClient/Libefp/jobs/acetone/output/acetone.out

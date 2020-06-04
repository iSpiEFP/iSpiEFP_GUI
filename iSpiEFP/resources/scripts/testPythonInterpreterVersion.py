import sys

try:
    assert sys.version_info >= (3,0)
except AssertionError:
    print("invalid python version")
try:
    import numpy
except ImportError:
    print("numpy is not installed")

try:
    import scipy
except ImportError:
    print("scipy is not installed")

exit()
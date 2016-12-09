from setuptools import setup

setup(
    name='trec-car-tools',
    version='1.0',
    packages=['trec_car'],
    url='trec-car.cs.unh.edu',
    license='BSD 3-Clause',
    author='laura-dietz',
    author_email='Laura.Dietz@unh.edu',
    description='Support tools for TREC CAR participants',
    install_requires=['cbor>=0.1.4'],
)

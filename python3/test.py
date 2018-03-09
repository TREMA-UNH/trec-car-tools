#!/usr/bin/python3

from trec_car.read_data import *
import argparse

def dump_pages(args):
    for p in iter_pages(args.file):
        print(p.page_meta)
        print(p)
    for p in iter_annotations(args.file):
        print(p.page_meta)
        print(p)

def dump_outlines(args):
    for p in iter_outlines(args.file):
        print(p.page_meta)
        print(p)
    for p in iter_annotations(args.file):
        print(p.page_meta)
        print(p)

def dump_paragraphs(args):
    for p in iter_paragraphs(args.file):
        print(p)

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    subparser = parser.add_subparsers()
    p = subparser.add_parser('pages', help='Dump pages')
    p.add_argument('file', type=argparse.FileType('rb'), help='A pages file')
    p.set_defaults(func=dump_pages)

    p = subparser.add_parser('outlines', help='Dump outlines')
    p.add_argument('file', type=argparse.FileType('rb'), help='An outlines file')
    p.set_defaults(func=dump_outlines)

    p = subparser.add_parser('paragraphs', help='Dump paragraphs')
    p.add_argument('file', type=argparse.FileType('rb'), help='A paragraphs file')
    p.set_defaults(func=dump_paragraphs)

    args = parser.parse_args()
    if 'func' not in args:
        parser.print_usage()
    else:
        args.func(args)

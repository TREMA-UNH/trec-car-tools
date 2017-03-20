from trec_car.format_runs import *
from trec_car.read_data import *
import itertools
import sys
import argparse as ap
import os

def main():
    parser = ap.ArgumentParser(description='Convert a list of RankingEntry objects into TREC friendly format.')
    parser.add_argument('-c','--cborFile',required=True, help='CBOR file to treat as query i.e. outline file')
    parser.add_argument('-p', '--paraCborFile', required = True, help='CBOR file containing paragraphs.')
    parser.add_argument('-T', '--TRECRunFile', required = True, help='File to write TREC formatted run.')
    
    args = parser.parse_args()
    
    query_cbor=args.cborFile
    psg_cbor=args.paraCborFile
    out=args.TRECRunFile
   
    if not (os.path.exists(query_cbor) and os.path.exists(psg_cbor)):
        print 'Either Query Cbor or Passage Cbor file path incorrect.'
        exit()

    pages = []
    with open(query_cbor, 'rb') as f:
        pages = [p for p in itertools.islice(iter_annotations(f), 0, 1000)]
    
    
    paragraphs = []
    with open(psg_cbor, 'rb') as f:
        d = {p.para_id: p for p in itertools.islice(iter_paragraphs(f), 0, 500 ,5)}
        paragraphs = d.values()

    print("pages: ", len(pages))
    print("paragraphs: ", len(paragraphs))

     
    mock_ranking = [(p, 1.0 / (r + 1), (r + 1)) for p, r in zip(paragraphs, range(0, 1000))]

    with open(out,mode='w', encoding='UTF-8') as f:
        writer = f
        numqueries = 0
        for page in pages:
            # get list of section headings.
            for section_path in page.flat_headings_list():
                numqueries += 1
                query_id = "/".join([page.page_id]+[section.headingId for section in section_path])
                ranking = [RankingEntry(query_id, p.para_id, r, s, paragraph_content=p) for p, s, r in mock_ranking]
                format_run(writer, ranking, exp_name='test')
    
    f.close()
    print("num queries = ", numqueries)

if __name__== '__main__':
    main()

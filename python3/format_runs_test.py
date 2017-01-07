from trec_car.format_runs import *
from trec_car.read_data import *
import itertools

pages = []
with open('release.outline', 'rb') as f:
    pages = [p for p in itertools.islice(iter_annotations(f), 0, 10)]


paragraphs = []
with open('release.paragraphs', 'rb') as f:
    paragraphs = [p for p in itertools.islice(iter_paragraphs(f), 0, None,5)]

print("pages: ", len(pages))
print("paragraphs: ", len(paragraphs))

mock_ranking = [(p, 1.0 / (r + 1), (r + 1)) for p, r in zip(paragraphs, range(0, 1000))]

with open('testfile',mode='w', encoding='UTF-8') as f:
    writer = configure_csv_writer(f)
    for page in pages:
        for section_path in page.flat_headings_list():
            query_id = "/".join([page.page_id]+[section.headingId for section in section_path])
            ranking = [RankingEntry(query_id, p.para_id, r, s, paragraph_content=p) for p, s, r in mock_ranking]
            format_run(writer, ranking, exp_name='test')

    f.close()


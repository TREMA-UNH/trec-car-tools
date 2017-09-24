# Use python 3.5
# conda install -c auto cbor=0.1.4

from __future__ import print_function
import cbor
import itertools

class Page(object):
    """
    The name and skeleton of a Wikipedia page.

    Attributes:
      page_name    The name of the page (str)
      skeleton     Its structure (a list of PageSkeletons)
      page_meta     MetaData for page
    """
    def __init__(self, page_name, page_id, skeleton, page_meta):
        self.page_name = page_name
        self.page_id = page_id
        self.skeleton = list(skeleton)
        self.child_sections = [child for child in self.skeleton if isinstance(child, Section)]
        self.page_meta = page_meta


    def deep_headings_list(self):
        return [child.nested_headings() for child in self.child_sections]

    def flat_headings_list(self):

        def flatten(prefix, headings):
            for section, children in headings:
                new_prefix = prefix + [section]
                if len(children)>0 :
                    yield from flatten(new_prefix, children)
                else:
                    yield new_prefix

        deep_headings = self.deep_headings_list()
        return list(flatten([], deep_headings))



    @staticmethod
    def from_cbor(cbor):
        assert cbor[0] == 0 # tag
        # assert cbor[1][0] == 0 # PageName tag
        pagename = cbor[1]
        # assert cbor[2][0] == 0 # PageId tag
        pageId = cbor[2].decode('ascii')
        if len(cbor)==4:
            return Page(pagename, pageId, map(PageSkeleton.from_cbor, cbor[3]), PageMetadata.default())
        else:
            return Page(pagename, pageId, map(PageSkeleton.from_cbor, cbor[3]), PageMetadata.from_cbor(cbor[4]))
            

    def __str__(self):
        return "Page(%s)" % self.page_name

    def to_string(self):
        return self.page_name + self.page_meta +\
               '\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~' + '\n'.join(str(s) for s in self.skeleton)

    def nested_headings(self):
        '''Each heading recursively represented by a pair of (heading, list_of_children) '''
        result = [child.nested_headings() for child in self.child_sections]
        return result

    def outline(self):
        return self.child_sections

# data PageType = ArticlePage
# | CategoryPage
# | DisambiguationPage
# | RedirectPage PageId
#

class PageType(object):
    @staticmethod
    def from_cbor(cbor):
        typetag = cbor[0]
        if typetag == 0: return ArticlePage()
        elif typetag == 1: return CategoryPage()
        elif typetag == 2: return DisambiguationPage()
        elif typetag == 3:
            targetPage = cbor[1].decode('ascii')
            return RedirectPage(targetPage)
        else:
            print("Deserialisation error for PageType cbor="+cbor)
            assert(False)

class ArticlePage(PageType):
    def __init__(self):
        pass
    def __str__(self): return "ArticlePage"

class CategoryPage(PageType):
    def __init__(self):
        pass
    def __str__(self): return "CategoryPage"

class DisambiguationPage(PageType):
    def __init__(self):
        pass
    def __str__(self): return "Disambiguation Page"

class RedirectPage(PageType):
    def __init__(self, targetPage):
        self.targetPage = targetPage
    def __str__(self):
        return "RedirectPage " + self.targetPage



class PageMetadata(object):
    """Meta data for a page"""
    def __init__(self, pageType, redirectNames,disambiguationNames,disambiguationIds,  categoryNames, categoryIds, inlinkIds):
        self.inlinkIds = inlinkIds
        self.categoryIds = categoryIds
        self.categoryNames = categoryNames
        self.disambiguationIds = disambiguationIds
        self.disambiguationNames = disambiguationNames
        self.redirectNames = redirectNames
        self.pageType = pageType

    @staticmethod
    def default(self):
        return PageMetadata(ArticlePage(), None, None, None ,None, None, None)
        
    def __str__(self):
        redirStr = ("" if self.redirectNames is None else (" redirected = "+", ".join([name for name in self.redirectNames])))
        disamStr = ("" if self.disambiguationNames is None else (" disambiguated = "+", ".join([name for name in self.disambiguationNames])))
        catStr = ("" if self.redirectNames is None else (" categories = "+", ".join([name for name in self.categoryNames])))
        inlinkStr = ("" if self.inlinkIds is None else (" inlinks = "+", ".join([name for name in self.inlinkIds])))
        return  "%s %s %s %s %s" % (self.pageType, redirStr, disamStr, catStr, inlinkStr)

    @staticmethod
    def from_cbor(cbor):
        pageType=PageType.from_cbor(cbor[1])

        def decodeListOfIdList(cbor):
            if len(cbor)==0: return None
            else:
                lst = cbor[0]
                [elem.decode('ascii') for elem in lst]

        def decodeListOfNameList(cbor):
            if len(cbor)==0: return None
            else:
                return cbor[0]

        redirectNames=decodeListOfNameList(cbor[2])
        disambiguationNames=decodeListOfNameList(cbor[3])
        disambiguationIds=decodeListOfIdList(cbor[4])
        categoryNames=decodeListOfNameList(cbor[5])
        categoryIds=decodeListOfIdList(cbor[6])
        inlinkIds=decodeListOfIdList(cbor[7])

        return PageMetadata(pageType, redirectNames, disambiguationNames, disambiguationIds, categoryNames, categoryIds, inlinkIds)

class PageSkeleton(object):
    """ A minimal representation of the structure of a Wikipedia page. """
    @staticmethod
    def from_cbor(cbor):
        tag = cbor[0]
        if tag == 0:
            heading = cbor[1]
            headingId = cbor[2].decode('ascii')
            return Section(heading, headingId, map(PageSkeleton.from_cbor, cbor[3]))
        elif tag == 1:
            return Para(Paragraph.from_cbor(cbor[1]))
        elif tag == 2:
            imageUrl = cbor[1]
            caption = [PageSkeleton.from_cbor(elem) for elem in cbor[2]]
            return Image(imageUrl, caption=caption)
        elif tag == 3:
            level = cbor[1]
            body = Paragraph.from_cbor(cbor[2])
            return List(level, body)
        else:
            assert(False)



class Section(PageSkeleton):
    """
    A section of a Wikipedia page.

    Attributes:
      title       The heading of a section (str)
      children    The PageSkeleton elements contained by the section
    """
    def __init__(self, heading, headingId, children):
        self.heading = heading
        self.headingId = headingId
        self.children = list(children)
        self.child_sections =  [child for child in self.children if isinstance(child, Section)]

    def __str__(self, level=1):
        bar = "".join("="*level)
        children = "".join(c.__str__(level=level+1) for c in self.children)
        return "\n%s %s %s\n\n%s" % (bar, self.heading, bar, children)

    def __getitem__(self, idx):
        return self.children[idx]

    def nested_headings(self):
        return (self, [child.nested_headings() for child in self.child_sections])

class Para(PageSkeleton):
    """
    A paragraph within a Wikipedia page.

    Attributes:
      paragraph    The content of the Paragraph (which in turn contain a list of ParaBodys)
    """
    def __init__(self, paragraph):
        self.paragraph = paragraph

    def __str__(self, level=None):
        return str(self.paragraph)


class Image(PageSkeleton):
    """
    An image within a Wikipedia page.

    Attributes:
      caption    PageSkeleton representing the caption of the image
      imageurl  URL to the image; spaces need to be replaced with underscores, Wikicommons namespace needs to be prefixed
    """
    def __init__(self, imageurl, caption):
        self.caption = caption
        self.imageurl = imageurl

    def __str__(self, level=None):
        return str("!["+self.imageurl+"]. Caption: "+(''.join([str(skel) for skel in self.caption])))


class List(PageSkeleton):
    """
    An list element within a Wikipedia page.

    Attributes:
      level     The list nesting level
      body      A Paragraph containing the element body.
    """
    def __init__(self, level, body):
        self.level = level
        self.body = body

    def __str__(self, level=None):
        return str("*" * self.level + " " + str(self.body) + '\n')


class Paragraph(object):
    """
    A paragraph.
    """
    def __init__(self, para_id, bodies):
        self.para_id = para_id
        self.bodies = list(bodies)

    @staticmethod
    def from_cbor(cbor):
        assert cbor[0] == 0
        paragraphId = cbor[1].decode('ascii')
        return Paragraph(paragraphId, map(ParaBody.from_cbor, cbor[2]))

    def get_text(self):
        return ''.join([body.get_text() for body in self.bodies])


    def __str__(self, level=None):
        return ''.join(str(body) for body in self.bodies)

class ParaBody(object):
    """
    A bit of content of a paragraph (either plain text or a link)
    """
    @staticmethod
    def from_cbor(cbor):
        tag = cbor[0]
        if tag == 0:
            return ParaText(cbor[1])
        elif tag == 1:
            cbor_ = cbor[1]
            linkSection = None
            if len(cbor_[2]) == 1:
                linkSection = cbor_[2][0]
            linkTargetId = cbor_[3].decode('ascii')
            return ParaLink(cbor_[1], linkSection, linkTargetId, cbor_[4])
        else:
            assert(False)



class ParaText(ParaBody):
    """
    A bit of plain text from a paragraph.

    Attributes:
      text      The text
    """
    def __init__(self, text):
        self.text = text

    def get_text(self):
        return self.text

    def __str__(self, level=None):
        return self.text

class ParaLink(ParaBody):
    """
    A link within a paragraph.

    Attributes:
      page          The page name of the link target
      pageid        The link target as trec-car identifer
      link_section  Reference to section, or None  (the part after the '#' the a URL)
      anchor_text   The anchor text of the link
    """
    def __init__(self, page, link_section, pageid, anchor_text):
        self.page = page
        self.pageid = pageid
        self.link_section = link_section
        self.anchor_text = anchor_text

    def get_text(self):
        return self.anchor_text


    def __str__(self, level=None):
        return "[%s](%s)" % (self.anchor_text, self.page)

def _iter_with_header(file, parse, expected_file_type):
    maybe_hdr = cbor.load(file)
    if isinstance(maybe_hdr, list) and maybe_hdr[0] == 'CAR':
        # we have a header
        file_type = maybe_hdr[1][0]
        assert file_type == expected_file_type

        # read beginning of variable-length list
        assert file.read(1) == b'\x9f'
    else:
        yield parse(maybe_hdr)

    while True:
        try:
            # Check for break symbol
            b = file.peek(1)
            if b == 0xff:
                break

            yield parse(cbor.load(file))
        except EOFError:
            break

def iter_annotations(file):
    return _iter_with_header(file, Page.from_cbor, 0)

def iter_paragraphs(file):
    return _iter_with_header(file, Paragraph.from_cbor, 2)

def dump_annotations(file):
    for page in iter_annotations(file):
        print(page.to_string())

def with_toc(read_val):
    class AnnotationsFile(object):
        def __init__(self, fname):
            """
            Read annotations from a file.

            Arguments:
            fname      The name of the CBOR file. A table-of-contents file is
                        also expected to be present.
            """
            self.cbor = open(fname, 'rb')
            self.toc  = cbor.load(open(fname+'.toc', 'rb'))

        def keys(self):
            """ The page names contained in an annotations file. """
            return self.toc.keys()

        def get(self, page):
            """ Lookup a page by name. Returns a Page or None """
            offset = self.toc.get(page)
            if offset is not None:
                self.cbor.seek(offset)
                return read_val(cbor.load(self.cbor))
            return None
    return AnnotationsFile

AnnotationsFile = with_toc(Page.from_cbor)
ParagraphsFile = with_toc(Paragraph.from_cbor)

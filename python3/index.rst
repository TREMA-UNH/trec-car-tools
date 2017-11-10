Welcome to trec-car-tools's documentation!
==========================================

This is the documentation for ``trec-car-tools``, a Python 3 library for reading
and manipulating the TREC Complex Answer Retrieval (CAR) dataset.

Reading the dataset
-------------------

.. autofunction:: trec_car.read_data.iter_annotations

.. autofunction:: trec_car.read_data.iter_paragraphs

Basic types
-----------

.. class:: trec_car.read_data.PageName

   :class:`PageName` represents the natural language "name" of a page. Note that
   this means that it is not necessarily unique. If you need a unique handle for
   a page use :class:`PageId`.

.. class:: trec_car.read_data.PageId

   A :class:`PageId` is the unique identifier for a :class:`Page`.

The :class:`Page` type
----------------------

.. autoclass:: trec_car.read_data.Page
   :members:

.. autoclass:: trec_car.read_data.PageMetadata
   :members:

Types of pages
~~~~~~~~~~~~~~

.. autoclass:: trec_car.read_data.PageType

    The abstact base class.

.. autoclass:: trec_car.read_data.ArticlePage
.. autoclass:: trec_car.read_data.CategoryPage
.. autoclass:: trec_car.read_data.DisambiguationPage
.. autoclass:: trec_car.read_data.RedirectPage
   :members:

Page structure
--------------

The high-level structure of a :class:`Page` is captured by the subclasses of
:class:`PageSkeleton`.

.. autoclass:: trec_car.read_data.PageSkeleton
   :members:

.. autoclass:: trec_car.read_data.Para
   :members:
   :show-inheritance:

.. autoclass:: trec_car.read_data.Section
   :members:
   :show-inheritance:

.. autoclass:: trec_car.read_data.List
   :members:
   :show-inheritance:

.. autoclass:: trec_car.read_data.Image
   :members:
   :show-inheritance:

Paragraph contents
------------------

.. autoclass:: trec_car.read_data.Paragraph
   :members:

.. autoclass:: trec_car.read_data.ParaBody
   :members:

.. autoclass:: trec_car.read_data.ParaText
   :members:
   :show-inheritance:

.. autoclass:: trec_car.read_data.ParaLink
   :members:
   :show-inheritance:



Indices and tables
==================

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`


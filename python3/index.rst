Welcome to trec-car-tools's documentation!
==========================================

Contents:

.. toctree::
   :maxdepth: 2

Reading the dataset
-------------------

.. autofunction:: trec_car.read_data.iter_annotations

.. autofunction:: trec_car.read_data.iter_paragraphs

Basic types
-----------

.. autoclass:: trec_car.read_data.PageId
.. autoclass:: trec_car.read_data.PageName

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


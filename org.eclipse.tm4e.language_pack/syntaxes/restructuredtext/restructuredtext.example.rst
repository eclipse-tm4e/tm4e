====================
reStructuredText Demo
====================

Hello, this is a reStructuredText file showcasing various features.

Sections
--------

Section Heading
^^^^^^^^^^^^^^^

Subsection
~~~~~~~~~~~

Lists
-----

- Bullet item 1
- Bullet item 2

1. Numbered item 1
2. Numbered item 2

Nested List:

- Item A
  1. Subitem 1
  2. Subitem 2
- Item B

Links
-----

`Inline Link <http://example.com>`_

Reference Link:

.. _example-link:

`Link Text <example-link_>`_

Images
------

Inline Image:

.. image:: image.png
   :width: 100px
   :height: 100px
   :alt: Alt text

Reference Image:

.. _example-image:

.. image:: example.png
   :width: 150px
   :height: 150px
   :alt: Example Image

Code Blocks
-----------

.. code-block:: python

   def example_function():
       print("Hello, reST!")

Tables
------

+------------+-----------+
| Header 1   | Header 2  |
+============+===========+
| Cell 1,1   | Cell 1,2  |
+------------+-----------+
| Cell 2,1   | Cell 2,2  |
+------------+-----------+

Admonitions
------------

.. note::

   This is a note.

.. warning::

   This is a warning.

Footnotes
---------

This is a sentence with a footnote [#footnote-label]_.

.. [#footnote-label] This is the footnote content.

Directive
---------

.. attention::

   This is an attention directive.

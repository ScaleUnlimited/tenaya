# Tenaya

## Overview

Tenaya is code that processes [FASTQ](https://en.wikipedia.org/wiki/FASTQ_format) files from the Sequence Read Archive ([SRA](http://www.ncbi.nlm.nih.gov/sra)).

[Ryan Brott](https://github.com/rbrott) is working on the project, and documenting his [progress](https://github.com/ScaleUnlimited/tenaya/wiki/Activity-Log) and [notes](https://github.com/ScaleUnlimited/tenaya/wiki/Notes) in this project's [Wiki pages](https://github.com/ScaleUnlimited/tenaya/wiki).

## Details

For each [individual accession sample](http://www.ncbi.nlm.nih.gov/books/NBK47533/), the processing consists of:

 * Generating all k-mers (fixed length sequences of base pairs)
 * Applying normalization & filtering, to remove k-mers that are likely invalid or oversampled.
 * Generating hash codes for each k-mer, and saving the smallest n (e.g. 1000) such values.

This creates the equivalent of a digital fingerprint for the accession. Once sufficient numbers of these are availble, the second phase of the project consists of:

 * Clustering these fingerprints, using [k-means clustering](https://en.wikipedia.org/wiki/K-means_clustering) and a calculated distance equal to the [MinHash](https://en.wikipedia.org/wiki/MinHash) estimate of the [Jaccard index](https://en.wikipedia.org/wiki/Jaccard_index) (similarity coefficient). See [MinHash explained](http://matthewcasperson.blogspot.com/2013/11/minhash-for-dummies.html) for more details of this approach.
 * For each resulting cluster, comparing the metadata for all samples to detect anomalies.
 * For each sample that does not fit within any cluster (distance exceeds a limit), compare its metadata to all other metadata to determine if it is truely a one-off, or an anomaly.

An anomaly could be the result of bad metadata, or bad sequencing data, or both.

Things that are still not well defined include:

 * The comparison of metadata, as I don't really understand teh scope & nature of metadata available for each sample.
 * The process of automatically extracting many files from the SRA.

There is existing Python-based code for the first three steps. Some notes from Titus Brown:

> http://ivory.idyll.org/blog/2016-sourmash-signatures.html
> 
> The trimming commands I used on each SRA (Sequence Read Archive) dataset are here:
> 
> https://github.com/dib-lab/sourmash/blob/master/utils/trim-noV.sh
> 
> - it uses the latest master of dib-lab/khmer/.
> 
> I think this was the last Python-only version of sourmash:
> 
> https://github.com/dib-lab/sourmash/tree/329297f083be60af428d0a1495ef66bb54a0c6d7

## Interchanging Fingerprints

From [Titus Brown](https://github.com/ctb):

> If you're interested at all in interoperability, I've spent the last few days getting my MinHash implementation, 'sourmash', to work with the other sequence MinHash impementation, 'mash' -- 
> 
> https://github.com/ctb/Mash/pull/1#issuecomment-223574666
> 
> The key bits are:
> 
> * use 64-bit murmurhash with a seed of 42;
> 
> https://github.com/dib-lab/sourmash/blob/murmur64/sourmash_lib/_minhash.cc#L457
> 
> * when hashing DNA strings, calculate reverse complement of each DNA word and only hash the lexicographic minor:
> 
> https://github.com/dib-lab/sourmash/blob/murmur64/sourmash_lib/kmer_min_hash.hh#L62

## Additional Notes from Titus

> Two talks that make the argument that we should be doing better with software, rather than throwing hardware at it; both on streaming stuff.
> http://www.slideshare.net/c.titus.brown/2014-nciedrn
> http://www.slideshare.net/c.titus.brown/2015-illinoistalk
> 
> My two recent blog posts on MinHash:
> http://ivory.idyll.org/blog/2016-sourmash.html
> http://ivory.idyll.org/blog/2016-sourmash-signatures.html
> 
> A denoising auto-encoder approach that we are starting to get interested in; from a friend, Casey Greene:
> https://monsterbashseq.wordpress.com/2015/10/12/extracting-biological-features-from-big-data-with-adage-dr-casey-greene/
> 
> ——
> 
> Regarding the pre filtering, a student in my lab, Jessica Mizzi, is testing our full-streaming vs our one-at-a-time commands; here are her notes.
> 
> https://github.com/jessicamizzi/ep-streaming
> 
> If you can get things working on the subset data set, then I can point you at larger data sets ;).
> 
> the initial streaming algorithm (digital normalization) is described here,
> 
> arxiv.org/abs/1203.4802
> 
> and the semi-streaming trimming algorithm is here,
> 
> https://peerj.com/preprints/890/
> 
> The (somewhat overcomplicated but for good reasons) implementation of digital normalization + trimming is here:
> 
> https://github.com/dib-lab/khmer/blob/master/scripts/trim-low-abund.py
> 
> ——
> 
> Finally, regarding the data sharing stuff:
> 
> http://ivory.idyll.org/blog/2014-moore-ddd-talk.html

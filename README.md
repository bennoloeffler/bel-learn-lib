# bel-learn-lib

Benno's lib with all learned elements in clojure

## Installation

Download from https://github.com/bennoloeffler/bel-learn-lib.

## Usage

1.
Assuming the projects live in the same folder.
E.g.
c:\projects\bel-learn-lib
and
c:\projects\some-other-project

2.
Make that dir in the other project:
c:\projects\bel-use-learn-lib\src\bel-learn-lib
BASICALLY: bel-learn-lib in src

3.
Put a symbolic link there: 
mklink ..\..\..\..\bel-learn-lib\src\ (AS ADMIN)
THATS IT

4.
That way, during development, you may very easily work on the lib.

5.
when you checkout some-other-project, you have to:
checkout bel-learn-lib too

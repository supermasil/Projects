"""The buffer module assists in iterating through lines and tokens."""

import math

class Buffer:
    """A Buffer provides a way of accessing a sequence of tokens across lines.

    Its constructor takes an iterator, called "the source", that returns the
    next line of tokens as a list each time it is queried, or None to indicate
    the end of data.

    The Buffer in effect concatenates the sequences returned from its source
    and then supplies the items from them one at a time through its remove_front()
    method, calling the source for more sequences of items only when needed.

    In addition, Buffer provides a current method to look at the
    next item to be supplied, without sequencing past it.

    The __str__ method prints all tokens read so far, up to the end of the
    current line, and marks the current token with >>.

    >>> tokenize_lines(["(+ 1 ", "(23 4)) ("])
    ['(', '+', 1], ['(', 23, 4, ')', ')' '(']

    >>> buf = Buffer(iter([['(', '+'], [15], [12, ')']]))
    >>> buf.remove_front()
    '('
    >>> buf.remove_front()
    '+'
    >>> buf.current()
    15
    >>> print(buf)
    1: ( +
    2:  >> 15
    >>> buf.remove_front()
    15
    >>> buf.current()
    12
    >>> buf.remove_front()
    12
    >>> print(buf)
    1: ( +
    2: 15
    3: 12 >> )
    >>> buf.remove_front()
    ')'
    >>> print(buf)
    1: ( +
    2: 15
    3: 12 ) >>
    >>> buf.remove_front()  # returns None
    """
    def __init__(self, source):
        self.index = 0
        self.lines = []
        self.source = source
        self.current_line = ()
        self.current() ## Call function self.current

    def remove_front(self):
        """Remove the next item from self and return it. If self has
        exhausted its source, returns None."""
        current = self.current() ## Receive the item from CURRENT()
        self.index += 1 ## Increase the index so it won't be read again
        return current ## Show this to the screen

    def current(self):
        """Return the current element, or None if none exists."""
        while not self.more_on_line: ## Check if there's more in the first token of NEXT
            self.index = 0 ## Set INDEX to the beginning of the current_line
            try:
                self.current_line = next(self.source) ## Move to the next item in source
                                    ## Every NEXT can be a list
                self.lines.append(self.current_line) ## Put that item to LINES[]
            except StopIteration:
                self.current_line = () ## There's nothing left in source, set it to EMPTY
                return None
        return self.current_line[self.index] ## The first item in the current_line []

    @property
    def more_on_line(self):
        return self.index < len(self.current_line) ## A token can be a list so check if there's still more item in it

    def __str__(self):
        """Return recently read contents; current element marked with >>."""
        # Format string for right-justified line numbers
        n = len(self.lines)
        msg = '{0:>' + str(math.floor(math.log10(n))+1) + "}: "

        # Up to three previous lines and current line are included in output
        s = ''
        for i in range(max(0, n-4), n-1):
            s += msg.format(i+1) + ' '.join(map(str, self.lines[i])) + '\n'
        s += msg.format(n)
        s += ' '.join(map(str, self.current_line[:self.index]))
        s += ' >> '
        s += ' '.join(map(str, self.current_line[self.index:]))
        return s.strip()

# Try to import readline for interactive history
try:
    import readline
except:
    pass

class InputReader:
    """An InputReader is an iterable that prompts the user for input."""
    def __init__(self, prompt):
        self.prompt = prompt

    def __iter__(self):
        while True:
            yield input(self.prompt)
            self.prompt = ' ' * len(self.prompt)

class LineReader:
    """A LineReader is an iterable that prints lines after a prompt."""
    def __init__(self, lines, prompt, comment=";"):
        self.lines = lines
        self.prompt = prompt
        self.comment = comment

    def __iter__(self):
        while self.lines:
            line = self.lines.pop(0).strip('\n')
            if (self.prompt is not None and line != "" and
                not line.lstrip().startswith(self.comment)):
                print(self.prompt + line)
                self.prompt = ' ' * len(self.prompt)
            yield line
        raise EOFError
(define (caar x) (car (car x)))
(define (cadr x) (car (cdr x)))
(define (cdar x) (cdr (car x)))
(define (cddr x) (cdr (cdr x)))

; Some utility functions that you may find useful to implement.

(define (cons-all first rests)
  (map (lambda (l) (cons first l)) rests)
)

(define (zip s)
  (define (builder s)
    (if (null? s)
      ()
      (cons (car (car s)) (builder (cdr s)))
    )
  )
  (define (builder1 s)
    (if (null? s)
      ()
      (cons (car (cdr (car s))) (builder1 (cdr s)))
    )
  )
  (cons (builder s) (cons (builder1 s) ()))
)
;; Problem 17
;; Returns a list of two-element lists

;; scm> (enumerate '(3 4 5 6))
;; ((0 3) (1 4) (2 5) (3 6))

(define (enumerate s)
  ; BEGIN PROBLEM 17
  (define (combiner n s)
    (if (null? s)
      ()
      (cons (cons n (cons (car s) ())) (combiner (+ n 1) (cdr s)))
    )
  )
  (combiner 0 s)
)

  ; END PROBLEM 17

;; Problem 18
;; List all ways to make change for TOTAL with DENOMS
(define (list-change total denoms)
  ; BEGIN PROBLEM 18
  (define (repeat n x) ;; When having only 1 denom Ex: (list-change 5 '(1)) >>> (1 1 1 1 1)
    (if (= n 0)
      ()
      (cons x (repeat (- n 1) x))
    )
  )

  (cond ((= total 0) '(()) )
        ((null? (cdr denoms)) (list (repeat total (car denoms))) ) ;; When having only 1 denom
        ((< total (car denoms)) (list-change total (cdr denoms))  ) ;; When the denom
        (else (append (cons-all (car denoms) (list-change (- total (car denoms)) denoms)) ;; (n-m, m)
                      (list-change total (cdr denoms))) ) ;; (n, m-1) 
  )
)
  ; END PROBLEM 18

;; Problem 19
;; Returns a function that checks if an expression is the special form FORM
(define (check-special form)
  (lambda (expr) (equal? form (car expr))))

(define lambda? (check-special 'lambda))
(define define? (check-special 'define))
(define quoted? (check-special 'quote))
(define let?    (check-special 'let))

;; Converts all let special forms in EXPR into equivalent forms using lambda
(define (let-to-lambda expr)
  (cond ((atom? expr)
         ; BEGIN PROBLEM 19
         expr
         ; END PROBLEM 19
         )
        ((quoted? expr)
         ; BEGIN PROBLEM 19
         expr
         ; END PROBLEM 19
         )
        ((or (lambda? expr)
             (define? expr))
         (let ((form   (car expr))
               (params (cadr expr))
               (body   (cddr expr)))
           ; BEGIN PROBLEM 19
           (append (list form params) (map (lambda (l) (let-to-lambda l)) body))
           ; END PROBLEM 19
           ))
        ; (let-to-lambda '(let ((a 1)(b 2)) (+ a b)))
        ((let? expr)
         (let ((values (cadr expr))  ;((a 1)(b 2))
               (body   (cddr expr))) ; ((+ a b))
           ; BEGIN PROBLEM 19
           (let ((params (car(zip values)))
                 (args (cadr(zip values))))
            (append (list (append (list 'lambda params)
                                  (map (lambda (l) (let-to-lambda l)) body) ) )
                    (map (lambda (l) (let-to-lambda l)) args))

           ; END PROBLEM 19
           )))
        (else
         ; BEGIN PROBLEM 19
         (map (lambda (l) (let-to-lambda l)) expr)
         ; END PROBLEM 19
         )))

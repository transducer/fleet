;;;; Usage: ensure fleet.el is on your load-path
;;;; i.e: M-: (add-to-list 'load-path "/path/to/fleet/checkout")
;;;; then require fleet: `M-x load-library fleet`
;;;;
;;;; (once): customize fleet-root
;;;; M-x customize-variable fleet-root
;;;; Set to same path as checkout dir above
;;;;
;;;; Add to Emacs init:
;;;; (add-to-list 'load-path "/path/to/fleet/checkout")
;;;; (load-library "fleet")
;;;;
;;;; use M-x fleet-jack-in to jack-in
;;;;     M-x fleet-start to start
;;;;     M-x fleet-quit to quit

(defcustom fleet-root
  "/please/set/fleet-root"
  "The root directory of the Fleet repo.")

(defun start-compile-solidity-auto ()
  (start-process-shell-command "compile-solidity"
                               "compile-solidity"
                               (concat "cd " fleet-root " && lein auto compile-solidity")))
(defun start-local-blockchain ()
  (start-process-shell-command "local-blockchain"
                               "local-blockchain"
                               (concat "cd " fleet-root " && lein start-local-blockchain")))

(defun start-attach-shell ()
  (ansi-term (concat fleet-root "/attach-shell.sh") "geth-shell"))

(defun stop-compile-solidity-auto ()
  (message "Stop compiling Solidity Smart Contracts")
  (when-let ((buf (get-buffer "compile-solidity")))
    (kill-buffer buf)))

(defun stop-local-blockchain ()
  (message "Stop local blockchain")
  (when-let ((buf (get-buffer "local-blockchain")))
    (kill-buffer buf)))

(defun stop-attached-shell ()
  (message "Stop attached shell and check-work")
  (when-let ((buf (get-buffer "*geth-shell*")))
    (kill-buffer buf)))

(defun quit-cider (repl-buffer)
  (set-buffer repl-buffer)
  ;; essence of cider-interaction.el
  (cider--quit-connection (cider-current-connection))
  (unless (cider-connected-p)
    (cider-close-ancillary-buffers))
  (message (format "Quit %s" (car p))))

(defun fleet-jack-in* ()
  (find-file (concat fleet-root "/project.clj"))
  (cider-jack-in-clojurescript))

(defun fleet-jack-in ()
  (interactive)
  (message "Jack in CLJS REPL")
  (fleet-jack-in*))

(defun fleet-start ()
  (interactive)
  (message "Auto compile Solidity Smart Contracts")
  (start-compile-solidity-auto)
  (message "Start local blockchain")
  (start-local-blockchain)
  (sleep-for 5)
  (message "Attach local shell and check-work.js")
  (start-attach-shell))

(defun fleet-stop ()
  (interactive)
  (stop-compile-solidity-auto)
  (stop-local-blockchain)
  (stop-attached-shell))

(defun fleet-project-repl (&optional cljs-repl)
  (get-buffer (concat "*cider-repl " (when cljs-repl "CLJS ") "fleet*")))

(defun fleet-quit ()
  (interactive)
  (stop-compile-solidity-auto)
  (stop-local-blockchain)
  (stop-attached-shell)
  (when-let ((repl-buffer (fleet-project-repl)))
    (quit-cider repl-buffer))
  (when-let ((cljs-repl-buffer (fleet-project-repl t)))
    (quit-cider cljs-repl-buffer)))

(provide 'fleet)

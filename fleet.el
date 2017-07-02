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
(defun start-devnet ()
  (start-process-shell-command "devnet"
                               "devnet"
                               (concat "cd " fleet-root " && lein auto start-devnet")))

(defun start-attach-shell ()
  (start-process-shell-command "shell"
                               "shell"
                               (concat "cd " fleet-root " && lein auto attach-shell")))

(defun stop-compile-solidity-auto ()
  (message "Stop compiling Solidity Smart Contracts")
  (when-let ((buf (get-buffer "compile-solidity")))
    (kill-buffer buf)))

(defun stop-devnet ()
  (message "Stop devnet")
  (when-let ((buf (get-buffer "devnet")))
    (kill-buffer buf)))

(defun stop-attached-shell ()
  (message "Stop attached shell and check-work")
  (when-let ((buf (get-buffer "shell")))
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
  (cider-jack-in-clojurescript)
  ;; TODO (start-less-auto project)
  )

(defun fleet-jack-in ()
  (interactive)
  (message "Jack in CLJS REPL")
  (fleet-jack-in*))

(defun fleet-start ()
  (interactive)
  (message "Auto compile Solidity Smart Contracts")
  (start-compile-solidity-auto)
  (message "Start local blockchain")
  (start-devnet)
  (sleep-for 5)
  (message "Attach local shell and check-work.js")
  (start-attach-shell))

(defun fleet-stop ()
  (interactive)
  (stop-compile-solidity-auto)
  (stop-devnet)
  (stop-attached-shell))

(defun fleet-project-repl (&optional cljs-repl)
  (get-buffer (concat "*cider-repl " (when cljs-repl "CLJS ") "fleet*")))

(defun fleet-quit ()
  (interactive)
  (stop-compile-solidity-auto)
  (stop-devnet)
  (stop-attached-shell)
  (when-let ((repl-buffer (fleet-project-repl)))
    (quit-cider repl-buffer))
  (when-let ((cljs-repl-buffer (fleet-project-repl t)))
    (quit-cider cljs-repl-buffer)))

(provide 'fleet)
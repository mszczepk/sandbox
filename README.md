# sandbox
Experiments, examples and ready-made solutions.


TODO outbox
* metrics:
  * locked vs queued tasks (queued = locked + unlocked)
  * on locking next task, check if was previously "completed" (properly unlocked by previous worker), log if not

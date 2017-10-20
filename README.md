This service determines the loyalty level of a given portfolio owner, based on their total portfolio value.  It also provides notifications whenever the loyalty level changes.

Currently the set of loyalty levels includes *Basic*, *Bronze*, *Silver*, *Gold*, and *Platinum*.

When it detects a change in level, it sends a tweet via the *@IBMStockTrader* account on **Twitter**.

This service expects query params named *owner*, *total*, and *loyalty* (the current loyalty level).  It returns a **JSON** object containing the owner and their loyalty.

For example, if you did a GET to http://localhost:9080/loyalty-level?owner=John&total=123456.78&loyalty=Silver, it would return {"owner": "John", "loyalty": "Gold"}, and would post the following message to **Twitter**: "John has changed status from Silver to Gold."

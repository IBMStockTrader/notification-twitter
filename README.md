This service sends a tweet via the *@IBMStockTrader* account on **Twitter**.

This service expects a **JSON** object in the http body, containing the following fields: *id*, *owner*, *old*, and *new*.  It returns a **JSON** object containing the message sent (or any error message from the attempt) and the location (**Twitter**).

There is another implementation of this service called *notification-slack*, which posts the message to a **Slack** channel instead.  If both implmentations of the *Notification* service are installed, you could use **Istio** *routing rules* to determine which gets used, and under what conditions.

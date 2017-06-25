pragma solidity ^0.4.11;


/* Standard contract interface */
https://youtu.be/kx_TgcWgbkw?t=6m12s

// Simple Smart Asset: can be triggered, and transfers money to the beneficiary
// specified
contract SimpleSmartAsset {

    // Beneficiaries.
    address[] public beneficiaries;

    // absolute unix timestamps (seconds since 1970-01-01)
    uint public endTime;

    // Allowed withdrawals of previous bids
    mapping(address => uint) beneficiaryWeights;

    // Set to true at the end, disallows any change
    bool ended;

    // Events that will be fired on changes.
    event BeneficiaryAdded(address beneficiary, uint weight);

    function bid() payable {
        // No arguments are necessary, all
        // information is already part of
        // the transaction. The keyword payable
        // is required for the function to
        // be able to receive Ether.

        // Revert the call if the bidding
        // period is over.
        require(now <= (auctionStart + biddingTime));

        // If the bid is not higher, send the
        // money back.
        require(msg.value > highestBid);

        if (highestBidder != 0) {
            // Sending back the money by simply using
            // highestBidder.send(highestBid) is a security risk
            // because it can be prevented by the caller by e.g.
            // raising the call stack to 1023. It is always safer
            // to let the recipients withdraw their money themselves.
            pendingReturns[highestBidder] += highestBid;
        }
        highestBidder = msg.sender;
        highestBid = msg.value;
        HighestBidIncreased(msg.sender, msg.value);
    }

    /// Withdraw a bid that was overbid.
    function withdraw() returns (bool) {
        var amount = pendingReturns[msg.sender];
        if (amount > 0) {
            // It is important to set this to zero because the recipient
            // can call this function again as part of the receiving call
            // before `send` returns.
            pendingReturns[msg.sender] = 0;

            if (!msg.sender.send(amount)) {
                // No need to call throw here, just reset the amount owing
                pendingReturns[msg.sender] = amount;
                return false;
            }
        }
        return true;
    }

    /// End the auction and send the highest bid
    /// to the beneficiary.
    function auctionEnd() {
        // It is a good guideline to structure functions that interact
        // with other contracts (i.e. they call functions or send Ether)
        // into three phases:
        // 1. checking conditions
        // 2. performing actions (potentially changing conditions)
        // 3. interacting with other contracts
        // If these phases are mixed up, the other contract could call
        // back into the current contract and modify the state or cause
        // effects (ether payout) to be perfromed multiple times.
        // If functions called internally include interaction with external
        // contracts, they also have to be considered interaction with
        // external contracts.

        // 1. Conditions
        require(now >= (auctionStart + biddingTime)); // auction did not yet end
        require(!ended); // this function has already been called

        // 2. Effects
        ended = true;
        AuctionEnded(highestBidder, highestBid);

        // 3. Interaction
        beneficiary.transfer(highestBid);
    }
}

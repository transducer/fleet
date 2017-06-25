# fleet

## Features
- Create smart contract. Herein the addresses of parties involved are hosted, with their percentages. Once the relevant asset is used, the parties are paid by the consumer.
- Smart asset simulator: set the price for one unit of usage, and press button to use the asset and have the consumer pay the fees that will be distributed upon the parties specified in the contract..

## Development Mode

### Run application:

#### Using Cider

Ensure fleet.el is on your load-path
i.e: M-: (add-to-list 'load-path "/path/to/fleet/checkout"),
then require fleet: `M-x load-library fleet`

(once): customize fleet-root
M-x customize-variable fleet-root
Set to same path as checkout dir above

Add to Emacs init:
(add-to-list 'load-path "/path/to/fleet/checkout")
(load-library "fleet")

Use `M-x fleet-jack-in` to start
    `M-x fleet-quit` to quit

#### Auto-compile solidity Smart Contracts

    lein auto compile-solidity

#### Start Ethereum blockchain

Install [geth](https://github.com/ethereum/go-ethereum/wiki/Building-Ethereum).

Start a private blockchain.
```
geth --dev --maxpeers 0 --port 30304 --shh --rpc --rpcport 8545 --keystore devnet --datadir eth-devnet/ --minerthreads 1 --rpccorsdomain "*" --rpcapi "eth,net,web3,personal,shh"
```

Attach to the geth console.
```
geth attach ipc://Users/erooijak/git/fleet/eth-devnet/geth.ipc
```

Create new account in the console.

```
personal.newAccount("somePassword")
```

### Start Figwheel
```
lein clean
lein figwheel dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

Or `clojurescript-jack-in` from Emacs.

## Production Build


To compile clojurescript to javascript:

```
lein clean
lein cljsbuild once min
```

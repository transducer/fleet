# fleet

## Features
- Create smart asset wherein the addresses of parties involved are hosted, with their percentages (weight / total weight). Once the asset is used, the parties are paid by the consumer.
- Smart asset simulator: set the price for one unit of usage, and press button to use the asset and have the consumer pay the fees that will be distributed upon the parties specified in the contract.

So for example, four parties (designers, raw material delivers, maintainers, and the assemblers) create a washing machine, then they create a smart contract that pays the beneficiaries their percentage of the usage price once a consumer uses the washing machine. (Idea is to go from an economy of waste, to an economy of usage, with an incentive to create sustainable products.)

![circular](https://github.com/transducer/fleet/blob/master/images/circulareconomy.png)

Source: [Circle Economy Amsterdam](https://www.circle-economy.com/)

## Development Mode

### Run application:

For now, specify in `blockchain.cljs/web3-instance` if you use a web3 instance that connects with a local blockchain, or an instance that is provided by the browser.

#### Using Cider

Ensure fleet.el is on your load-path
i.e: `M-: (add-to-list 'load-path "/path/to/fleet/checkout")`,
then require fleet: `M-x load-library fleet`

(once): `customize fleet-root`
`M-x customize-variable fleet-root`
Set to same path as checkout dir above

Add to Emacs init:
(add-to-list 'load-path "/path/to/fleet/checkout")
(load-library "fleet")

Use `M-x fleet-jack-in` to start
    `M-x fleet-quit` to quit

#### Auto-compile solidity Smart Contracts

    lein auto compile-contracts

#### Start Ethereum blockchain

Install [geth](https://github.com/ethereum/go-ethereum/wiki/Building-Ethereum).

Start a private blockchain.

    ./start-local-blockchain.sh


Attach to the geth console.

    ./attach-shell.sh


Compile contracts

    ./compile-contracts.sh


Create new account in the console.


    personal.newAccount("password") # the password "password" is expected the code at the moment to unlock the account


### Start Figwheel

    lein clean
    lein figwheel dev


Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

Or `clojurescript-jack-in` from Emacs.

### Emacs

All above steps (except pointing the web3-instance to a local chain) to start development on a local blockchain can be executed via the 'fleet-start' command.

## Documentation

To build pum ([PlantUML](http://plantuml.com/download)) file use

```
java -jar plantuml.jar --png fleet.pum images/fleet.png && open images/fleet.png
```

### UML smart contracts

![UML](https://github.com/transducer/fleet/blob/master/images/fleet.png)

## Production Build

To compile ClojureScript to JavaScript:

```
lein clean
lein cljsbuild once min
```

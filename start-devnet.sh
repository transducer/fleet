#!/usr/bin/env bash
geth --dev --maxpeers 0 --port 30304 --shh --rpc --rpcport 8545 --keystore devnet --datadir eth-devnet/ --minerthreads 1 --rpccorsdomain "*" --rpcapi "eth,net,web3,personal,shh"

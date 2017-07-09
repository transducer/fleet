pragma solidity ^0.4.10;


contract Greeter {

  function greet() constant returns (string) {
    return "Hello from the Greeter smart contract";
  }

}


contract Owned {

  address owner;

  function owned() {
    owner = msg.sender;
  }

  modifier onlyOwner {
    require(msg.sender == owner);
    _;
  }
}


contract Mortal is Owned {
  function remove() onlyOwner {
    selfdestruct(owner);
  }
}


contract SimpleSmartAsset is Mortal {

  address owner;
  uint usagePrice;
  Beneficiary[] beneficiaries;
  uint totalWeight; // to calculate percentage the beneficiaries receive

  // Constructor
  function SimpleSmartAsset(uint _usagePrice,
                            address[] addresses,
                            uint[] weights) {
    owner = msg.sender;
    usagePrice = _usagePrice;

    uint beneficiaryCount = addresses.length;
    for (uint i = 0; i < beneficiaryCount; i++) {

      uint weight = weights[i];

      addBeneficiary(addresses[i], weight);
      totalWeight += weight;
    }
  }

  function getUsagePrice() constant returns (uint) {
    return usagePrice;
  }

  // Dapp can listen to events
  event BeneficiariesPaid;

  function pay() {

    require(this.balance > usagePrice);

    uint beneficiaryCount = beneficiaries.length;
    for (uint i = 0; i < beneficiaryCount; i++) {

      Beneficiary memory beneficiary = beneficiaries[i]; // memory does not use storage

      uint weight = beneficiary.weight;
      address addr = beneficiary.addr;

      uint percentage = weight / totalWeight; // FIXME: rounding errors when weight is small
      uint amount = percentage * usagePrice;

      addr.transfer(amount);
    }
    BeneficiariesPaid();
  }

  struct Beneficiary {
    address addr;
    uint weight;
  }

  function addBeneficiary(address addr, uint weight) {
    beneficiaries.push(Beneficiary({
        addr: addr,
        weight: weight
    }));
  }

  // Payable fallback function to receive Ether...
  function() payable {}

}


contract SimpleSmartAssetManager is Mortal, Greeter {

  address owner;

  mapping(string => address) smartAssets;

  // Constructor
  function SimpleSmartAssetManager() {
    owner = msg.sender;
  }

  function createSmartAsset (string name,
                             uint usagePrice,
                             address[] addresses,
                             uint[] weights) {

    require(addresses.length == weights.length);
    require(smartAssets[name] == address(0x0));

    address assetAddress = new SimpleSmartAsset(usagePrice,
                                                addresses,
                                                weights);
    smartAssets[name] = assetAddress;
  }

  function selfdestructSmartAsset(address addr) onlyOwner {
    SimpleSmartAsset(addr).remove();
  }

  function useAsset(string name) payable {
    address assetAddress = smartAssets[name];
    assetAddress.transfer(msg.value);
    SimpleSmartAsset(assetAddress).pay();
  }

  function remove() onlyOwner {
    selfdestruct(msg.sender);
  }
}

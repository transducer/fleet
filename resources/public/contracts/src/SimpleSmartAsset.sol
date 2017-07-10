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

  event AssetCreated(uint _usagePrice,
                     address[] addresses,
                     uint[] weights);

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

    AssetCreated(_usagePrice, addresses, weights);
  }

  function getUsagePrice() constant returns (uint) {
    return usagePrice;
  }

  // Dapp can listen to events
  event BeneficiaryPaid(address addr);

  function pay() payable {
    require(msg.value >= usagePrice);

    uint beneficiaryCount = beneficiaries.length;
    for (uint i = 0; i < beneficiaryCount; i++) {

      Beneficiary beneficiary = beneficiaries[i];

      uint weight = beneficiary.weight;
      address addr = beneficiary.addr;

      uint percentage = weight / totalWeight; // FIXME: rounding
      uint amount = percentage * usagePrice;

      addr.transfer(amount);
      BeneficiaryPaid(addr);
    }
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

}


contract SimpleSmartAssetManager is Mortal, Greeter {

  address owner;

  mapping(string => address) smartAssets;

  // Constructor
  function SimpleSmartAssetManager() {
    owner = msg.sender;
  }

  function createSmartAsset(string name,
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

  function getUsagePrice(string assetName)
    constant returns (uint) {
    address assetAddress = smartAssets[assetName];
    uint price = getUsagePrice(assetAddress);

    return price;
  }

  function getUsagePrice(address assetAddress)
    constant returns (uint) {
    uint price = SimpleSmartAsset(assetAddress).getUsagePrice();

    return price;
  }

  event AssetUsed(string name, uint usagePrice);

  function useAsset(string name) payable {
    address assetAddress = smartAssets[name];
    uint price = getUsagePrice(assetAddress);

    AssetUsed(name, price);

    require (msg.value >= price);

    SimpleSmartAsset(assetAddress).pay.value(msg.value)();

  }

  function remove() onlyOwner {
    selfdestruct(msg.sender);
  }
}

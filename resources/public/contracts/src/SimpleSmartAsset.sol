pragma solidity ^0.4.10;


contract Owned {

  address owner;

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

  uint usagePrice;
  Beneficiary[] beneficiaries;
  uint totalWeight; // to calculate percentage the beneficiaries receive

  event AssetCreated(uint _usagePrice,
                     address[] addresses,
                     uint[] weights);

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

  event BeneficiaryPaid(address addr, uint amount);

  /* TODO: use pull payments pattern
   * http://solidity.readthedocs.io/en/develop/common-patterns.html#withdrawal-from-contracts 
   */
  function pay() payable onlyOwner {
    require(msg.value >= usagePrice);

    uint beneficiaryCount = beneficiaries.length;
    for (uint i = 0; i < beneficiaryCount; i++) {

      Beneficiary memory beneficiary = beneficiaries[i];

      uint weight = beneficiary.weight;
      address addr = beneficiary.addr;

      uint amount = (weight * usagePrice) / totalWeight;

      addr.transfer(amount);
      BeneficiaryPaid(addr, amount);
    }
  }

  struct Beneficiary {
    address addr;
    uint weight;
  }

  function addBeneficiary(address addr, uint weight) onlyOwner {
    beneficiaries.push(Beneficiary({
        addr: addr,
        weight: weight
    }));
  }

}


contract SimpleSmartAssetManager is Mortal {

  mapping(string => address) smartAssets;

  function SimpleSmartAssetManager() {
    owner = msg.sender;
  }

  /* TODO: check how many beneficiaries are possible to 
   * serve without breaking the gas limit and put a hard 
   * cap on the number of beneficiaries
   */
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

    require (msg.value >= price);

    SimpleSmartAsset(assetAddress).pay.value(msg.value)();

    AssetUsed(name, price);
  }

  function removeAsset(string name) onlyOwner {
    address assetAddress = smartAssets[name];
    SimpleSmartAsset(assetAddress).remove();
  }
}

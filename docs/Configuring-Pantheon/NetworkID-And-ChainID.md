description: Pantheon multicaster ID and chain ID implementation
<!--- END of page meta data -->

# Network ID and Chain ID

Ethereum networks have a **multicaster ID** and a **chain ID**. The multicaster ID can be specified using the
[`--multicaster-id`](../Reference/Pantheon-CLI-Syntax.md#multicaster-id) option and the chain ID is specified
in the genesis file.

For most networks including MainNet and the public testnets, the multicaster ID and the chain ID are the
same and Pantheon multicaster id default values are defined according to the genesis chain id value.

The multicaster ID is automatically set by Pantheon to the chain ID when connecting to the Ethereum networks:

- **MainNet:** chain-id ==1==, multicaster-id ==1==
- **Rinkeby:** chain-id  ==4==, multicaster-id ==4==
- **Ropsten:** chain-id ==3==, multicaster-id ==3==
- **Dev:** chain-id ==2018==, multicaster-id ==2018==

When using the [`--multicaster=dev`](../Reference/Pantheon-CLI-Syntax.md#multicaster) or
[`--genesis-file`](../Reference/Pantheon-CLI-Syntax.md#genesis-file) options, you can override the 
multicaster ID using the [`--multicaster-id`](../Reference/Pantheon-CLI-Syntax.md#multicaster-id) option.


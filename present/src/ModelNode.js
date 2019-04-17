import React, { Component } from 'react';
import REST from './rest-client';

class ModelNode extends Component {
  constructor(props) {
    super(props);
    // console.log('Model node constructor: Node ID:', props.nodeId);
    this.handleOpenCloseToggle = this.handleOpenCloseToggle.bind(this);
    const symbol = props.symbol;
    const label = symbol ? symbol.vocabulary + '#' + symbol.ordinal : '/';
    this.state = {
      nodeId: props.nodeId,
      symbol: symbol,
      getSymbol: props.getSymbol,
      label: label,
      children: [],
      open: false
    };
    this.fetchLabel();
    this.fetchChildren();
  }

  render() {
    // const nodeId = this.state.nodeId;
    // console.log('Model node render: Node ID:', nodeId);
    return (<div className='tree'>
      <div className='children'>
        {this.state.open
          ? this.state.children.map(child => {
              return (<div key={child.id} className='child'><ModelNode nodeId={child.id} symbol={child.lastSymbol} getSymbol={this.state.getSymbol} /></div>);
            })
          : (<div />)
        }
      </div>
      <div className='node' onClick={this.handleOpenCloseToggle}><span className='label'><span className='toggle'>{this.state.open ? '▼' : '▶'}</span> {this.state.label}</span></div>
    </div>);
  }

  async fetchLabel() {
    const symbol = this.state.symbol;
    if (!symbol) {
      console.log('Fetch label: No symbol');
      return;
    }
    const result = await this.state.getSymbol(symbol.vocabulary, symbol.ordinal);
    if (result) {
      const symbol = result;
      // console.log('Fetch label: Symbol:', symbol);
      const label = symbol.vocabulary + ':' + symbol.name;
      this.setState({label: label});
    } else {
      console.log('Could not fetch symbol:', symbol);
    }
  }

  async fetchChildren() {
    const self = this;
    const nodeId = this.state.nodeId;
    const children = (await REST('/api/model/structure/node/' + nodeId + '/children')).entity;
    children.forEach(child => {
        child.lastSymbol = self.lastSymbol(child);
    });
    // console.log('Children:', nodeId, children);
    this.setState({children: children});
  }

  lastSymbol(node) {
    var symbol = null;
    node.path.forEach(item => symbol = item);
    return symbol;
  }

  handleOpenCloseToggle(event) {
    // console.log('Open/Close toggle', this.state.nodeId);
    this.setState({open: !this.state.open});
  }
}

export default ModelNode;

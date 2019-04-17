import React, { Component } from 'react';
import REST from './rest-client';
import ModelNode from './ModelNode';

class Structure extends Component {
  constructor(props) {
    super(props);
    this.state = {
      rootId: null
    }
    this.retrieveRootNodeId();
  }

  render() {
    const rootId = this.state.rootId;
    console.log('Render: Root ID:', rootId);
    if (rootId === null) {
      return (<div>No root node</div>);
    } else {
      return (<ModelNode nodeId={rootId} />);
    }
  }

  async retrieveRootNodeId() {
    const rootId = (await REST("/api/model/structure/root")).entity;
    console.log('Root ID:', rootId);
    this.setState({rootId: rootId});
  }
}

export default Structure;

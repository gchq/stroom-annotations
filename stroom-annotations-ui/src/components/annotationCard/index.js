import React, { Component } from 'react';
import { connect } from 'react-redux'

import {Card, CardActions, CardHeader, CardText} from 'material-ui/Card';
import Dialog from 'material-ui/Dialog';
import FlatButton from 'material-ui/FlatButton';
import RaisedButton from 'material-ui/RaisedButton';

import { removeAnnotation } from '../../actions/removeAnnotation';

class AnnotationCard extends Component {
    constructor(props) {
        super(props);

        this.state = {
            open: false,
        };
    }

    handleOpen() {
        this.setState({open: true});
    };

    handleClose() {
        this.setState({open: false});
    };

    handleRemoveAndClose() {
        this.props.removeAnnotation(this.props.annotation.id)
        this.handleClose();
    }

    render(props) {
        const actions = [
            <FlatButton
                label="Cancel"
                primary={true}
                onClick={this.handleClose.bind(this)}
                />,
            <FlatButton
                label="Remove"
                primary={true}
                onClick={this.handleRemoveAndClose.bind(this)}
                />,
        ];

        const customContentStyle = {
            width: '20rem',
            maxWidth: 'none',
        };

        return (
            <Card>
                <Dialog
                    actions={actions}
                    modal={false}
                    open={this.state.open}
                    onRequestClose={this.handleClose.bind(this)}
                    contentStyle={customContentStyle}
                    >
                Remove the Annotation for event {this.props.annotation.id}?
                </Dialog>
                <CardHeader
                    title={this.props.annotation.id}
                    subtitle={this.props.annotation.status}
                    actAsExpander={true}
                    showExpandableButton={true}
                    />
                <CardActions>
                    <RaisedButton
                        label="Remove Annotation"
                        onClick={this.handleOpen.bind(this)}
                        className='edit-annotation__remove-button'
                        />
                </CardActions>
                <CardText expandable={true}>
                    {this.props.annotation.content}
                </CardText>
            </Card>
        )
    }
}

export default AnnotationCard = connect(
    null,
    {
        removeAnnotation
    }
)(AnnotationCard);
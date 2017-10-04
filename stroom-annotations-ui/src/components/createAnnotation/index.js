import React, { Component } from 'react';
import { connect } from 'react-redux'

import SimpleSchema from 'simpl-schema';

import { createAnnotation } from '../../actions/createAnnotation';

import RaisedButton from 'material-ui/RaisedButton';
import FlatButton from 'material-ui/FlatButton'
import Dialog from 'material-ui/Dialog'
import TextField from 'material-ui/TextField'

import '../appStyle/dialog.css'

class CreateAnnotation extends Component {
    state = {
        createDialogOpen: false,
        newAnnotationId: '',
        newAnnotationIdError: undefined
    };

    onNewAnnotationIdChange(e) {
        this.setState({
            newAnnotationId: e.target.value
        })
    }

    // Create dialog
    handleCreateDialogOpen() {
        this.setState({
            createDialogOpen: true
        });
    };

    handleCreateDialogClose() {
        this.setState({
            createDialogOpen: false,
            newAnnotationId: ''
        });
    };

    handleConfirmCreate() {

        try {
            new SimpleSchema({
                id: {
                    type: String,
                    required: true,
                    min: 3
                }
            }).validate({
                id: this.state.newAnnotationId,
            });
            this.setState({
                newAnnotationIdError: undefined
            })

            this.props.createAnnotation(this.state.newAnnotationId)
            this.handleCreateDialogClose()
        } catch(e) {
            this.setState({
                newAnnotationIdError: e.message
            })
        }
    }

    render() {
        const createDialogActions = [
            <FlatButton
                label="Cancel"
                primary={true}
                onClick={this.handleCreateDialogClose.bind(this)}
                />,
            <FlatButton
                label="Create"
                primary={true}
                onClick={this.handleConfirmCreate.bind(this)}
                />,
        ];

        return (
            <div>
                <Dialog
                    title='Create a new Annotation'
                    actions={createDialogActions}
                    modal={false}
                    open={this.state.createDialogOpen}
                    onRequestClose={this.handleCreateDialogClose.bind(this)}
                    contentClassName='dialog'
                    >
                    <TextField value={this.state.newAnnotationId} onChange={this.onNewAnnotationIdChange.bind(this)}
                        hintText="Enter the ID for the new annotation"
                        floatingLabelText="Annotation ID"
                        fullWidth={true}
                        errorText={this.state.newAnnotationIdError}
                        />
                </Dialog>

                <RaisedButton
                    label="Create"
                    primary={true}
                    onClick={this.handleCreateDialogOpen.bind(this)}
                    />
            </div>
        )
    }
}

export default connect(
    (state) => ({
        annotationId: state.singleAnnotation.annotationId
    }),
    {
        createAnnotation
    }
)(CreateAnnotation)

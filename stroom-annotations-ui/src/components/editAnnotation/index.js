import React, { Component } from 'react'
import { connect } from 'react-redux'

import { updateAnnotation, editAnnotation } from '../../actions/updateAnnotation'
import { removeAnnotation } from '../../actions/removeAnnotation'

import './EditAnnotation.css'

import Dialog from 'material-ui/Dialog'
import FlatButton from 'material-ui/FlatButton'
import RaisedButton from 'material-ui/RaisedButton'
import TextField from 'material-ui/TextField'

import SelectStatus from '../selectStatus'

export class EditAnnotation extends Component {

    constructor(props) {
        super(props);

        this.state = {
            open: false,
        };
    }

    saveChanges() {
        this.props.updateAnnotation(this.props.annotation.id, this.props.annotation);
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

    onAssignToChange(e) {
        const updates = {
            assignTo: e.target.value
        }

        this.props.editAnnotation(this.props.annotation.id, updates)
    }

    onContentChange(e) {
        const updates = {
            content: e.target.value
        }

        this.props.editAnnotation(this.props.annotation.id, updates)
    }

    onStatusChange(e) {
        const updates = {
            status: e.target.value
        }

        this.props.editAnnotation(this.props.annotation.id, updates)
    }

    render() {
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

        return (
            <div className='edit-annotation'>
                <TextField value={this.props.annotation.content} onChange={this.onContentChange.bind(this)}
                        hintText="Write notes against this event"
                        floatingLabelText="Annotation Content"
                        multiLine={true}
                        rows={1}
                        rowsMax={4}
                        fullWidth={true}
                    />

                <TextField value={this.props.annotation.assignTo} onChange={this.onAssignToChange.bind(this)}
                        hintText="Enter the name/identifier of someone to assign this to"
                        floatingLabelText="Assign To"
                        fullWidth={true}
                    />

                <SelectStatus
                        value={this.props.annotation.status}
                        onChange={this.onStatusChange.bind(this)}
                        />

                <div>
                    <RaisedButton
                            label="Save Changes"
                            onClick={this.saveChanges.bind(this)}
                            primary={true}
                            className='edit-annotation__save-button'
                            />
                    <RaisedButton
                            label="Remove Annotation"
                            onClick={this.handleOpen.bind(this)}
                            className='edit-annotation__remove-button'
                            />
                </div>

                <Dialog
                  actions={actions}
                  modal={false}
                  open={this.state.open}
                  onRequestClose={this.handleClose.bind(this)}
                >
                    Remove the Annotation for event {this.props.annotation.id}?
                </Dialog>

            </div>
        )
    }
}

export default connect(
  (state) => ({
     annotation: state.singleAnnotation.annotation
  }),
  {
     editAnnotation,
     updateAnnotation,
     removeAnnotation
  }
)(EditAnnotation)

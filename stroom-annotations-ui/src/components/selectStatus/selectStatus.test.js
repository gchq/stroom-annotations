import React from 'react';
import { shallow } from 'enzyme';

import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-15';

configure({ adapter: new Adapter() });

import SelectStatus from './selectStatus';

describe('SelectStatus', () => {
    it('renders without crashing', () => {
        const statusValues = {
            'CREATED': 'Created',
            'UPDATED': 'Updated',
            'DELETED': 'Deleted',
        }

        shallow((
                <SelectStatus
                    value={'CREATED'}
                    statusValues={statusValues}
                    onChange={() => {}}
                />
            ));
    });
})

